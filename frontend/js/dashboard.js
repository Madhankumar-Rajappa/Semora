/**
 * CampusTrack — Academic Dashboard Controller
 * Displays academic summaries: total semesters, active term, subjects count, SGPA, and cumulative CGPA.
 * Communicates strictly with Java REST API endpoints.
 */
document.addEventListener('DOMContentLoaded', () => {

    let sgpaChartInstance = null;

    // DOM Elements - KPI Summary
    const dashTotalSemesters = document.getElementById('dashTotalSemesters');
    const dashSemestersSubtext = document.getElementById('dashSemestersSubtext');
    const dashCurrentTerm = document.getElementById('dashCurrentTerm');
    const dashCurrentYear = document.getElementById('dashCurrentYear');
    const dashTotalSubjects = document.getElementById('dashTotalSubjects');
    const dashTotalCredits = document.getElementById('dashTotalCredits');
    const dashCgpaValue = document.getElementById('dashCgpaValue');
    const dashCgpaSubtext = document.getElementById('dashCgpaSubtext');

    // Progression & Containers
    const fourYearPillsContainer = document.getElementById('fourYearPillsContainer');
    const courseProgressContainer = document.getElementById('courseProgressContainer');
    const activeSemesterCourseSubtitle = document.getElementById('activeSemesterCourseSubtitle');
    const semesterSummaryTableBody = document.getElementById('semesterSummaryTableBody');

    // Ensure authenticated session
    Auth.requireAuth();
    Auth.initNavbar();

    // Status & Indicators
    const apiStatusDot = document.getElementById('apiStatusDot');
    const apiStatusText = document.getElementById('apiStatusText');

    checkBackendHealth();
    loadDashboardData();

    // =========================================================================
    // 1. HEALTH CHECK
    // =========================================================================

    async function checkBackendHealth() {
        try {
            const health = await ApiClient.get('/health');
            if (health && (health.status === 'UP' || health.database === 'CONNECTED')) {
                if (apiStatusDot) apiStatusDot.className = 'status-dot-pulse bg-success';
                if (apiStatusText) apiStatusText.textContent = 'API Connected';
            }
        } catch (err) {
            if (apiStatusDot) apiStatusDot.className = 'status-dot-pulse bg-danger';
            if (apiStatusText) apiStatusText.textContent = 'Offline';
        }
    }

    // =========================================================================
    // 2. DASHBOARD DATA AGGREGATION & RENDERING
    // =========================================================================

    async function loadDashboardData() {
        try {
            // 1. Load all semesters for student
            const semestersRes = await ApiClient.get('/semesters');
            const semesters = Array.isArray(semestersRes) ? semestersRes : [];

            // Sort semesters in ascending order
            semesters.sort((a, b) => (a.semesterNumber || 0) - (b.semesterNumber || 0));

            // 2. Load academic summaries for all semesters in parallel
            const summaryPromises = semesters.map(sem => {
                const sid = sem.id || sem.semesterId;
                return ApiClient.get(`/semesters/${sid}/academic-summary`)
                    .catch(err => {
                        console.warn(`Could not load summary for semester ${sid}:`, err);
                        return null;
                    });
            });

            const summaries = await Promise.all(summaryPromises);

            // Pair summaries with semesters
            const semesterRecords = semesters.map((sem, idx) => ({
                semester: sem,
                summary: summaries[idx] || null
            }));

            // 3. Render Dashboard Components
            renderKpisAndCgpa(semesterRecords);
            renderTimelinePills(semesterRecords);
            renderSgpaChart(semesterRecords);
            renderSummaryTable(semesterRecords);

            // 4. Load Active Semester Coursework
            const activeRecord = semesterRecords.find(r => (r.semester.status || '').toUpperCase() === 'ACTIVE') || semesterRecords[semesterRecords.length - 1];
            if (activeRecord) {
                const activeId = activeRecord.semester.id || activeRecord.semester.semesterId;
                const semName = activeRecord.semester.semesterName || `Semester ${activeRecord.semester.semesterNumber}`;
                if (activeSemesterCourseSubtitle) {
                    activeSemesterCourseSubtitle.textContent = `${semName} enrolled coursework and scores`;
                }
                loadActiveSemesterCoursework(activeId);
            } else {
                if (courseProgressContainer) {
                    courseProgressContainer.innerHTML = '<div class="text-center py-4 text-muted small">No active semester enrolled.</div>';
                }
            }

        } catch (err) {
            console.error('[Dashboard] Error loading data:', err);
            if (semesterSummaryTableBody) {
                semesterSummaryTableBody.innerHTML = `
                    <tr>
                        <td colspan="8" class="text-center py-4 text-danger">
                            <i class="bi bi-exclamation-triangle me-1"></i> Failed to load dashboard data: ${escapeHtml(err.message)}
                        </td>
                    </tr>
                `;
            }
        }
    }

    // Calculate CGPA and KPI Metrics
    function renderKpisAndCgpa(records) {
        const totalSemesters = records.length;
        let completedCount = 0;
        let activeCount = 0;
        let totalSubjectsCount = 0;
        let totalRegisteredCredits = 0;

        let totalEvaluatedCredits = 0;
        let totalWeightedPoints = 0.0;

        let activeSemester = null;

        records.forEach(r => {
            const isCompleted = (r.semester.status || '').toUpperCase() === 'COMPLETED';
            if (isCompleted) completedCount++;
            else activeCount++;

            if (!activeSemester && !isCompleted) {
                activeSemester = r.semester;
            }

            if (r.summary) {
                totalSubjectsCount += (r.summary.subjectCount || 0);
                totalRegisteredCredits += (r.summary.totalCredits || 0);

                // Cumulative CGPA calculation: sum(SGPA * evaluatedCredits) / sum(evaluatedCredits)
                if (r.summary.sgpa !== null && r.summary.sgpa !== undefined) {
                    const sgpa = parseFloat(r.summary.sgpa);
                    const evalCredits = r.summary.evaluatedCredits || r.summary.earnedCredits || 0;
                    if (evalCredits > 0 && !isNaN(sgpa)) {
                        totalEvaluatedCredits += evalCredits;
                        totalWeightedPoints += (sgpa * evalCredits);
                    }
                }
            }
        });

        // If no active semester found, pick latest
        if (!activeSemester && records.length > 0) {
            activeSemester = records[records.length - 1].semester;
        }

        // Render KPI 1: Total Semesters
        if (dashTotalSemesters) dashTotalSemesters.textContent = totalSemesters;
        if (dashSemestersSubtext) dashSemestersSubtext.textContent = `${completedCount} Completed • ${activeCount} Active`;

        // Render KPI 2: Current Term
        if (activeSemester) {
            const termName = activeSemester.semesterName || `Semester ${activeSemester.semesterNumber}`;
            if (dashCurrentTerm) dashCurrentTerm.textContent = termName;
            if (dashCurrentYear) dashCurrentYear.textContent = activeSemester.academicYear || 'Academic Year Pending';
        } else {
            if (dashCurrentTerm) dashCurrentTerm.textContent = 'None Active';
            if (dashCurrentYear) dashCurrentYear.textContent = 'Enroll in a semester';
        }

        // Render KPI 3: Registered Subjects
        if (dashTotalSubjects) dashTotalSubjects.textContent = totalSubjectsCount;
        if (dashTotalCredits) dashTotalCredits.textContent = `${totalRegisteredCredits} Total Credits Registered`;

        // Render KPI 4: Cumulative CGPA
        if (totalEvaluatedCredits > 0) {
            const cgpa = totalWeightedPoints / totalEvaluatedCredits;
            if (dashCgpaValue) dashCgpaValue.textContent = cgpa.toFixed(2);
            if (dashCgpaSubtext) dashCgpaSubtext.textContent = `${totalEvaluatedCredits} credits evaluated across terms`;
        } else {
            if (dashCgpaValue) dashCgpaValue.textContent = '—';
            if (dashCgpaSubtext) dashCgpaSubtext.textContent = 'Awaiting completed semester grades';
        }
    }

    // Render 4-Year Timeline Banner
    function renderTimelinePills(records) {
        if (!fourYearPillsContainer) return;
        fourYearPillsContainer.innerHTML = '';

        for (let i = 1; i <= 8; i++) {
            const record = records.find(r => r.semester.semesterNumber === i);
            const a = document.createElement('a');

            if (record) {
                const sid = record.semester.id || record.semester.semesterId;
                const isCompleted = (record.semester.status || '').toUpperCase() === 'COMPLETED';
                a.href = `semester-detail.html?id=${sid}`;
                a.className = `btn btn-sm rounded-pill px-3 py-1 fw-bold ${isCompleted ? 'timeline-pill-completed' : 'timeline-pill-active'}`;
                a.style.fontSize = '0.82rem';
                a.innerHTML = `<i class="bi ${isCompleted ? 'bi-check-circle-fill' : 'bi-play-circle-fill'} me-1"></i> Sem ${i}`;
            } else {
                a.href = 'semesters.html';
                a.className = 'btn btn-sm rounded-pill px-3 py-1 timeline-pill-empty';
                a.style.fontSize = '0.82rem';
                a.innerHTML = `<i class="bi bi-circle me-1 opacity-50"></i> Sem ${i}`;
            }

            fourYearPillsContainer.appendChild(a);
        }
    }

    // Render SGPA Progression Chart (Chart.js)
    function renderSgpaChart(records) {
        const canvas = document.getElementById('sgpaTrendChart');
        if (!canvas) return;

        const chartRecords = records.filter(r => r.summary && r.summary.sgpa !== null && r.summary.sgpa !== undefined);

        const labels = chartRecords.map(r => `Sem ${r.semester.semesterNumber}`);
        const dataPoints = chartRecords.map(r => parseFloat(r.summary.sgpa));

        if (sgpaChartInstance) {
            sgpaChartInstance.destroy();
        }

        const ctx = canvas.getContext('2d');

        if (chartRecords.length === 0) {
            // Draw placeholder text on canvas
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            ctx.fillStyle = '#64748b';
            ctx.font = '14px Outfit, sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('No final semester grades recorded yet to display trend.', canvas.width / 2, canvas.height / 2);
            return;
        }

        sgpaChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Semester SGPA',
                    data: dataPoints,
                    borderColor: '#0d9488',
                    backgroundColor: 'rgba(13, 148, 136, 0.1)',
                    borderWidth: 3,
                    fill: true,
                    tension: 0.35,
                    pointBackgroundColor: '#0f766e',
                    pointBorderColor: '#ffffff',
                    pointBorderWidth: 2,
                    pointRadius: 6,
                    pointHoverRadius: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        min: 0,
                        max: 10,
                        ticks: {
                            stepSize: 2,
                            callback: (v) => v.toFixed(1)
                        },
                        grid: {
                            color: '#f1f5f9'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => ` SGPA: ${ctx.parsed.y.toFixed(2)} / 10.0`
                        }
                    }
                }
            }
        });
    }

    // Render Active Semester Coursework List
    async function loadActiveSemesterCoursework(semId) {
        if (!courseProgressContainer) return;

        try {
            const subjects = await ApiClient.get(`/semesters/${semId}/subjects`);
            courseProgressContainer.innerHTML = '';

            if (!subjects || subjects.length === 0) {
                courseProgressContainer.innerHTML = '<div class="text-center py-4 text-muted small">No courses registered in this semester yet.</div>';
                return;
            }

            subjects.forEach(sub => {
                const item = document.createElement('div');
                item.className = 'p-3 bg-light rounded-3 border mb-2';

                const subCode = sub.subjectCode || '—';
                const subName = sub.subjectName || 'Course';
                const credits = sub.credits !== undefined ? sub.credits : 3;
                const assessCount = sub.assessmentCount || 0;
                const totalMarks = sub.totalObtainedMarks || 0;
                const maxMarks = sub.totalMaxMarks || 0;
                const pct = maxMarks > 0 ? (totalMarks / maxMarks) * 100 : 0;

                const result = sub.result || null;
                const gradeBadge = (result && result.finalGrade) ? 
                    `<span class="badge badge-grade-${result.finalGrade.replace('+', 'Plus')} ms-auto">Grade ${result.finalGrade}</span>` : 
                    `<span class="badge bg-light text-secondary border ms-auto small">In Progress</span>`;

                item.innerHTML = `
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <div class="d-flex align-items-center gap-2">
                            <span class="subject-code-pill small">${escapeHtml(subCode)}</span>
                            <span class="fw-bold text-dark small">${escapeHtml(subName)}</span>
                            <span class="badge bg-light text-secondary border small">${credits} Cr</span>
                        </div>
                        ${gradeBadge}
                    </div>
                    <div class="d-flex justify-content-between align-items-center text-muted small mb-1" style="font-size: 0.75rem;">
                        <span><i class="bi bi-clipboard2-check text-teal-custom me-1"></i>${assessCount} continuous assessments</span>
                        <span>${assessCount > 0 ? `${pct.toFixed(1)}%` : 'No marks yet'}</span>
                    </div>
                    <div class="progress" style="height: 5px;">
                        <div class="progress-bar ${pct >= 75 ? 'bg-teal-custom' : (pct >= 50 ? 'bg-primary' : 'bg-warning')}" 
                             role="progressbar" style="width: ${Math.min(100, pct)}%;"></div>
                    </div>
                `;

                courseProgressContainer.appendChild(item);
            });

        } catch (err) {
            console.error('[Dashboard] Error loading active coursework:', err);
            courseProgressContainer.innerHTML = '<div class="text-center py-4 text-muted small">Unable to load coursework for active semester.</div>';
        }
    }

    // Render Semesters Summary Table
    function renderSummaryTable(records) {
        if (!semesterSummaryTableBody) return;
        semesterSummaryTableBody.innerHTML = '';

        if (!records || records.length === 0) {
            semesterSummaryTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-4 text-muted">
                        No semesters registered yet. Click "Add Semester" to begin.
                    </td>
                </tr>
            `;
            return;
        }

        records.forEach(r => {
            const tr = document.createElement('tr');
            const sid = r.semester.id || r.semester.semesterId;
            const semName = r.semester.semesterName || `Semester ${r.semester.semesterNumber}`;
            const academicYear = r.semester.academicYear || '—';
            const isCompleted = (r.semester.status || '').toUpperCase() === 'COMPLETED';
            const statusBadge = isCompleted ? 
                '<span class="badge bg-success bg-opacity-25 text-success px-2.5 py-1">Completed</span>' : 
                '<span class="badge bg-primary bg-opacity-25 text-primary px-2.5 py-1">Active</span>';

            const summary = r.summary || {};
            const subjectCount = summary.subjectCount || 0;
            const totalCredits = summary.totalCredits || 0;
            const evaluatedCredits = summary.evaluatedCredits || summary.earnedCredits || 0;

            let sgpaDisplay = '—';
            if (summary.sgpa !== null && summary.sgpa !== undefined) {
                const sgpaVal = parseFloat(summary.sgpa).toFixed(2);
                const isProv = summary.isProvisional !== false;
                sgpaDisplay = `
                    <span class="fw-bold text-dark fs-6">${sgpaVal}</span>
                    <span class="badge ${isProv ? 'bg-warning bg-opacity-25 text-dark' : 'bg-success bg-opacity-25 text-success'} small ms-1" style="font-size: 0.7rem;">
                        ${isProv ? 'Prov' : 'Final'}
                    </span>
                `;
            }

            tr.innerHTML = `
                <td>
                    <div class="d-flex align-items-center gap-2">
                        <div class="sem-number-pill" style="width: 32px; height: 32px; font-size: 0.9rem;">
                            ${r.semester.semesterNumber}
                        </div>
                        <span class="fw-bold text-dark">${escapeHtml(semName)}</span>
                    </div>
                </td>
                <td class="text-secondary small">
                    ${escapeHtml(academicYear)}
                </td>
                <td>
                    ${statusBadge}
                </td>
                <td class="text-center fw-semibold text-dark">
                    ${subjectCount}
                </td>
                <td class="text-center text-secondary">
                    ${totalCredits}
                </td>
                <td class="text-center text-secondary">
                    ${evaluatedCredits}
                </td>
                <td class="text-center">
                    ${sgpaDisplay}
                </td>
                <td class="text-end">
                    <a href="semester-detail.html?id=${sid}" class="btn btn-outline-secondary btn-sm rounded-pill px-3">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Open Workspace
                    </a>
                </td>
            `;

            semesterSummaryTableBody.appendChild(tr);
        });
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }
});
