/**
 * CampusTrack — Modern UpAhead-Inspired Dashboard & 4-Year Collegiate Life Tracker
 */
document.addEventListener('DOMContentLoaded', async () => {
    // 1. Authentication & Session Validation
    Auth.requireAuth();

    const currentUser = ApiClient.getUser() || {
        id: 1,
        name: 'Alex Morgan',
        email: 'alex.morgan@university.edu',
        department: 'Computer Science & Engineering',
        admissionYear: 2022
    };

    // User Profile in Sidebar
    const userAvatarEl = document.getElementById('userAvatarLetter');
    const userNameEl = document.getElementById('sidebarUserName');
    const userEmailEl = document.getElementById('sidebarUserEmail');
    const logoutBtn = document.getElementById('sidebarLogoutBtn');

    if (userAvatarEl) userAvatarEl.textContent = (currentUser.name || 'A').charAt(0).toUpperCase();
    if (userNameEl) userNameEl.textContent = currentUser.name || 'Student';
    if (userEmailEl) userEmailEl.textContent = currentUser.email || 'student@university.edu';
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            Auth.logout();
        });
    }

    // 2. Theme Toggle (Light / Dark)
    const themeToggleBtn = document.getElementById('themeToggleBtn');
    const themeIcon = document.getElementById('themeIcon');
    const themeText = document.getElementById('themeText');

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('campustrack_theme', theme);
        if (theme === 'dark') {
            if (themeIcon) themeIcon.className = 'bi bi-sun-fill me-1';
            if (themeText) themeText.textContent = 'Light';
            if (themeToggleBtn) {
                themeToggleBtn.classList.remove('btn-outline-secondary');
                themeToggleBtn.classList.add('btn-outline-warning');
            }
        } else {
            if (themeIcon) themeIcon.className = 'bi bi-moon-stars-fill me-1';
            if (themeText) themeText.textContent = 'Dark';
            if (themeToggleBtn) {
                themeToggleBtn.classList.remove('btn-outline-warning');
                themeToggleBtn.classList.add('btn-outline-secondary');
            }
        }
    }

    const savedTheme = localStorage.getItem('campustrack_theme') || 'light';
    applyTheme(savedTheme);

    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
            const nextTheme = currentTheme === 'dark' ? 'light' : 'dark';
            applyTheme(nextTheme);
            ApiClient.showToast(`Switched to ${nextTheme === 'dark' ? 'Dark Mode' : 'Light Mode'}`, 'info');
        });
    }

    // 3. Role Switcher Pill (Student vs Lab Evaluator)
    const roleStudentBtn = document.getElementById('roleStudentBtn');
    const roleAdminBtn = document.getElementById('roleAdminBtn');

    if (roleStudentBtn && roleAdminBtn) {
        roleStudentBtn.addEventListener('click', () => {
            roleStudentBtn.classList.add('active');
            roleAdminBtn.classList.remove('active');
            localStorage.setItem('campustrack_role', 'student');
            ApiClient.showToast('Student view activated.', 'info');
        });

        roleAdminBtn.addEventListener('click', () => {
            roleAdminBtn.classList.add('active');
            roleStudentBtn.classList.remove('active');
            localStorage.setItem('campustrack_role', 'admin');
            ApiClient.showToast('Lab Evaluator & Faculty Inspection mode active.', 'success');
        });
    }

    // State Store for Deliverables and Filtering
    let allAssignments = [];
    let dashboardAnalyticsData = null;
    let activeFilterStatus = 'ALL';
    let isExamsOnly = false;
    let showCompleted = false;
    let gaugeMode = 'weekly'; // 'weekly' or 'semester'

    // Chart references
    let sgpaChartInstance = null;
    let statusDoughnutInstance = null;

    // Course Color Palette for consistent branding
    const courseColorMap = {
        'PHYS 211': '#8b5cf6', // Violet
        'TE 200': '#3b82f6',   // Blue
        'MATH 241': '#ec4899', // Pink / Magenta
        'CS 501': '#3b82f6',
        'CS 502': '#06b6d4',
        'CS 503': '#10b981',
        'CS 504': '#f59e0b',
        'CS 508': '#8b5cf6'
    };

    function getCourseColor(code) {
        if (!code) return '#64748b';
        return courseColorMap[code] || '#6366f1';
    }

    // 4. Initial Load
    await loadDashboard();
    await loadAssignments();

    async function loadDashboard() {
        try {
            const data = await ApiClient.get('/analytics/dashboard');
            dashboardAnalyticsData = data;
            render4YearRoadmap(data.fourYearRoadmap, data.degreeAudit);
            renderCourseProgress(data.courseProgress);
            renderUpcomingExams(data.upcomingExams);
            renderProgressGauge();
            renderCharts(data);
        } catch (err) {
            console.warn('Backend analytics failed, using robust fallback state:', err);
            useMockDashboardData();
        }
    }

    // 5. 4-Year Journey & Degree Audit Rendering
    function render4YearRoadmap(roadmap, degreeAudit) {
        const container = document.getElementById('fourYearPillsContainer');
        if (!container) return;

        // Degree Audit Sidebar Card
        if (degreeAudit) {
            const percent = degreeAudit.completionPercent || 72;
            const earned = degreeAudit.earnedCredits || 115;
            const total = degreeAudit.totalRequiredCredits || 160;

            const badgeEl = document.getElementById('degreePercentBadge');
            const fillEl = document.getElementById('degreeProgressFill');
            const credEl = document.getElementById('degreeCreditsEarned');

            if (badgeEl) badgeEl.textContent = `${percent}%`;
            if (fillEl) fillEl.style.width = `${percent}%`;
            if (credEl) credEl.textContent = `${earned}/${total} Credits`;
        }

        const fallbackRoadmap = [
            { yearNumber: 1, yearTitle: '1st Year (Freshman)', semestersText: 'Sem 1 & 2', status: 'COMPLETED', badgeColor: 'success', credits: 46 },
            { yearNumber: 2, yearTitle: '2nd Year (Sophomore)', semestersText: 'Sem 3 & 4', status: 'COMPLETED', badgeColor: 'success', credits: 46 },
            { yearNumber: 3, yearTitle: '3rd Year (Junior)', semestersText: 'Sem 5 & 6', status: 'IN PROGRESS', badgeColor: 'primary', credits: 36 },
            { yearNumber: 4, yearTitle: '4th Year (Senior)', semestersText: 'Sem 7 & 8', status: 'UPCOMING', badgeColor: 'secondary', credits: 32 }
        ];

        const list = roadmap && roadmap.length ? roadmap : fallbackRoadmap;

        container.innerHTML = list.map(item => {
            const isActive = item.status === 'IN PROGRESS';
            const icon = item.status === 'COMPLETED' ? 'bi-check-circle-fill text-success' :
                         isActive ? 'bi-play-circle-fill text-primary' : 'bi-lock-fill text-muted';
            const borderStyle = isActive 
                ? 'border: 2px solid var(--accent-primary) !important;' 
                : 'border: 1px solid var(--border-color) !important;';

            return `
                <div class="px-3 py-2 rounded-3 shadow-sm d-flex align-items-center gap-2" 
                     style="font-size: 0.82rem; background-color: var(--bg-card); color: var(--text-primary); ${borderStyle}">
                    <i class="bi ${icon} fs-6"></i>
                    <div>
                        <div class="fw-bold lh-1" style="color: var(--text-primary);">${escapeHtml(item.yearTitle)}</div>
                        <span class="text-muted" style="font-size: 0.72rem;">${escapeHtml(item.semestersText)} &bull; <strong>${item.credits} Cr</strong></span>
                    </div>
                    <span class="badge bg-${item.badgeColor}-subtle text-${item.badgeColor} ms-1" style="font-size: 0.68rem; font-weight: 700;">
                        ${item.status}
                    </span>
                </div>
            `;
        }).join('');
    }

    // 6. Circular Progress Gauge
    function renderProgressGauge() {
        const valEl = document.getElementById('gaugeMainVal');
        const subLabelEl = document.getElementById('gaugeSubLabel');
        const footerTextEl = document.getElementById('gaugeFooterText');
        const arcEl = document.getElementById('gaugeProgressArc');

        // Circumference of r=54 circle is 2 * PI * 54 = 339.29
        const circumference = 339.29;

        if (gaugeMode === 'weekly') {
            // Count assignments due this week vs completed this week
            const weekDue = allAssignments.filter(a => {
                const status = calculateStatus(a);
                return status === 'This Week' || status === 'Tomorrow' || status === 'Overdue';
            });
            const weekCompleted = weekDue.filter(a => a.status === 'Completed').length;
            const totalWeek = weekDue.length || 3;
            const ratio = totalWeek > 0 ? (weekCompleted / totalWeek) : 0;

            if (valEl) valEl.textContent = `${weekCompleted}/${totalWeek}`;
            if (subLabelEl) subLabelEl.textContent = 'Due This Week';
            if (footerTextEl) {
                const remaining = totalWeek - weekCompleted;
                footerTextEl.innerHTML = remaining === 0
                    ? '<i class="bi bi-trophy text-success me-1"></i>All weekly deliverables finished!'
                    : `<i class="bi bi-clock-history text-primary me-1"></i>${remaining} deliverable${remaining > 1 ? 's' : ''} left this week`;
            }

            if (arcEl) {
                const offset = circumference * (1 - ratio);
                arcEl.style.strokeDashoffset = offset.toFixed(2);
                arcEl.style.stroke = ratio === 1 ? '#10b981' : 'var(--accent-primary)';
            }
        } else {
            // Semester Progress
            const totalAss = allAssignments.length || 8;
            const compAss = allAssignments.filter(a => a.status === 'Completed').length;
            const ratio = totalAss > 0 ? (compAss / totalAss) : 0.6;

            if (valEl) valEl.textContent = `${compAss}/${totalAss}`;
            if (subLabelEl) subLabelEl.textContent = 'Semester Tasks Done';
            if (footerTextEl) {
                footerTextEl.innerHTML = `<i class="bi bi-mortarboard text-info me-1"></i>Semester 5 Curriculum Completion: ${Math.round(ratio * 100)}%`;
            }

            if (arcEl) {
                const offset = circumference * (1 - ratio);
                arcEl.style.strokeDashoffset = offset.toFixed(2);
                arcEl.style.stroke = '#3b82f6';
            }
        }
    }

    // Gauge Mode Switcher
    const progWeeklyTab = document.getElementById('progWeeklyTab');
    const progSemesterTab = document.getElementById('progSemesterTab');

    if (progWeeklyTab && progSemesterTab) {
        progWeeklyTab.addEventListener('click', () => {
            gaugeMode = 'weekly';
            progWeeklyTab.classList.add('active');
            progSemesterTab.classList.remove('active');
            renderProgressGauge();
        });

        progSemesterTab.addEventListener('click', () => {
            gaugeMode = 'semester';
            progSemesterTab.classList.add('active');
            progWeeklyTab.classList.remove('active');
            renderProgressGauge();
        });
    }

    // 7. Course Progress Card Rendering (Matching Reference)
    function renderCourseProgress(courses) {
        const container = document.getElementById('courseProgressContainer');
        if (!container) return;

        const defaultCourses = [
            { subjectCode: 'PHYS 211', progressPercent: 33, dueThisWeek: 1, completedCount: 1, totalCount: 3, color: '#8b5cf6' },
            { subjectCode: 'TE 200', progressPercent: 86, dueThisWeek: 2, completedCount: 18, totalCount: 21, color: '#3b82f6' },
            { subjectCode: 'MATH 241', progressPercent: 0, dueThisWeek: 0, completedCount: 0, totalCount: 5, color: '#ec4899' }
        ];

        const list = (courses && courses.length) ? courses : defaultCourses;

        container.innerHTML = list.map(item => {
            const color = item.color || getCourseColor(item.subjectCode);
            const dueText = item.dueThisWeek > 0 ? `${item.dueThisWeek} due this week` : `On Track`;
            const countText = `${item.completedCount || 0}/${item.totalCount || item.credits || 3}`;

            return `
                <div class="course-prog-item">
                    <div class="course-item-top">
                        <div class="d-flex align-items-center">
                            <div class="course-badge-icon" style="background-color: ${color};">
                                <i class="bi bi-journal-bookmark-fill"></i>
                            </div>
                            <span class="course-title-text">${escapeHtml(item.subjectCode)}</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="course-stat-text">${dueText}</span>
                            <button class="btn btn-link btn-sm text-muted p-0 text-decoration-none course-close-btn" title="Dismiss" style="font-size: 0.9rem;">
                                &times;
                            </button>
                        </div>
                    </div>
                    <div class="course-bar">
                        <div class="course-bar-fill" style="width: ${item.progressPercent || 30}%; background-color: ${color};"></div>
                    </div>
                    <div class="d-flex justify-content-end text-muted" style="font-size: 0.72rem; font-weight: 600;">
                        <span>${countText}</span>
                    </div>
                </div>
            `;
        }).join('');

        // Wire close button dismissals
        container.querySelectorAll('.course-close-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const itemEl = btn.closest('.course-prog-item');
                if (itemEl) {
                    itemEl.style.opacity = '0.3';
                    ApiClient.showToast('Course widget view minimized.', 'info');
                }
            });
        });
    }

    // 8. Upcoming Exams Card Rendering (Matching Reference)
    function renderUpcomingExams(exams) {
        const container = document.getElementById('upcomingExamsContainer');
        if (!container) return;

        const defaultExams = [
            { type: 'Final', subjectCode: 'MATH 241', date: 'Jan 6' },
            { type: 'Exam 1', subjectCode: 'PHYS 211', date: 'Jan 13' },
            { type: 'Midterm', subjectCode: 'CS 501', date: 'Nov 18' }
        ];

        const list = (exams && exams.length) ? exams : defaultExams;

        container.innerHTML = list.map(item => {
            const dateStr = formatDateBadge(item.date);
            return `
                <div class="exam-item">
                    <span class="exam-type-pill">${escapeHtml(item.type || 'Exam')}</span>
                    <div class="exam-course-tag">
                        <i class="bi bi-folder-fill text-muted"></i>
                        <span>${escapeHtml(item.subjectCode || 'General')}</span>
                    </div>
                    <div class="exam-date-badge">
                        <i class="bi bi-calendar-event me-1"></i>${dateStr}
                    </div>
                </div>
            `;
        }).join('');
    }

    function formatDateBadge(dateVal) {
        if (!dateVal) return 'Upcoming';
        const d = new Date(dateVal);
        if (isNaN(d.getTime())) return String(dateVal);
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }

    // 9. Load Assignments & Deliverables
    async function loadAssignments() {
        try {
            const list = await ApiClient.get('/assignments');
            allAssignments = (list && list.length) ? list : getReferenceDeliverables();
        } catch (e) {
            console.warn('API /assignments unavailable, loading reference dataset:', e);
            allAssignments = getReferenceDeliverables();
        }
        renderTable();
        renderProgressGauge();
    }

    // Reference Deliverables (Strictly Matching UpAhead Prototype)
    function getReferenceDeliverables() {
        return [
            {
                id: 101,
                title: 'Innovator Project',
                status: 'Pending',
                type: 'exam',
                subjectCode: 'TE 200',
                dueDate: '2026-12-30T10:00:00',
                priority: 'URGENT',
                description: 'Final semester prototype demonstration'
            },
            {
                id: 102,
                title: 'You as an Innovator Assignment',
                status: 'Pending',
                type: 'Pre-class Activity',
                subjectCode: 'TE 200',
                dueDate: '2026-01-01T23:59:00',
                priority: 'HIGH',
                description: 'Self-reflection and career roadmapping'
            },
            {
                id: 103,
                title: 'physics check #1',
                status: 'In Progress',
                type: 'physics',
                subjectCode: 'PHYS 211',
                dueDate: '2026-01-02T16:00:00',
                priority: 'MEDIUM',
                description: 'Newtonian mechanics problem set'
            },
            {
                id: 104,
                title: 'final',
                status: 'Pending',
                type: 'study session',
                subjectCode: 'MATH 241',
                dueDate: '2026-01-06T18:00:00',
                priority: 'HIGH',
                description: 'Multivariable calculus group revision'
            },
            {
                id: 105,
                title: 'study session #2',
                status: 'Pending',
                type: 'study session',
                subjectCode: 'MATH 241',
                dueDate: '2026-01-07T17:00:00',
                priority: 'MEDIUM',
                description: 'Vectors and partial derivatives practice'
            },
            {
                id: 106,
                title: 'Exam 1',
                status: 'Pending',
                type: 'physics',
                subjectCode: 'PHYS 211',
                dueDate: '2026-01-13T10:00:00',
                priority: 'HIGH',
                description: 'Kinematics and rotational dynamics'
            },
            {
                id: 107,
                title: 'Attendance and Participation',
                status: 'In Progress',
                type: 'Attendance and Partici...',
                subjectCode: 'TE 200',
                dueDate: null,
                priority: 'LOW',
                description: 'Weekly seminar contribution tracking'
            }
        ];
    }

    // 10. Status Pill Calculator
    function calculateStatus(item) {
        if (item.status === 'Completed') return 'Completed';
        if (!item.dueDate) return 'No Date';

        const now = new Date();
        const due = new Date(item.dueDate);
        const diffMs = due.getTime() - now.getTime();
        const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));

        if (diffDays < 0) return 'Overdue';
        if (diffDays <= 1) return 'Tomorrow';
        if (diffDays <= 7) return 'This Week';
        return 'Future';
    }

    function formatShortDate(dateStr) {
        if (!dateStr) return 'No due d...';
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        const mm = String(d.getMonth() + 1).padStart(2, '0');
        const dd = String(d.getDate()).padStart(2, '0');
        return `${mm}/${dd}`;
    }

    // 11. Render Assignment Overview Table
    function renderTable() {
        const tbody = document.getElementById('referenceTableBody');
        if (!tbody) return;

        const searchQuery = (document.getElementById('tableSearchInput')?.value || '').toLowerCase().trim();

        const filtered = allAssignments.filter(item => {
            // Search query filter
            if (searchQuery) {
                const titleMatch = (item.title || '').toLowerCase().includes(searchQuery);
                const courseMatch = (item.subjectCode || '').toLowerCase().includes(searchQuery);
                const typeMatch = (item.type || '').toLowerCase().includes(searchQuery);
                if (!titleMatch && !courseMatch && !typeMatch) return false;
            }

            // Show Completed Toggle
            if (!showCompleted && item.status === 'Completed') {
                return false;
            }

            // Exams Only Filter
            if (isExamsOnly) {
                const typeStr = (item.type || '').toLowerCase();
                const titleStr = (item.title || '').toLowerCase();
                const isExam = typeStr.includes('exam') || titleStr.includes('exam') || typeStr.includes('test') || titleStr.includes('final');
                if (!isExam) return false;
            }

            // Dropdown Status Filter
            if (activeFilterStatus !== 'ALL') {
                if (item.status !== activeFilterStatus) return false;
            }

            return true;
        });

        if (filtered.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center py-5 text-muted">
                        <i class="bi bi-search fs-2 d-block mb-2 text-secondary opacity-50"></i>
                        No deliverables found matching your filter criteria.
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = filtered.map(item => {
            const relStatus = calculateStatus(item);
            const isCompleted = item.status === 'Completed';
            const courseColor = getCourseColor(item.subjectCode);
            const statusClass = relStatus.replace(/\s+/g, '');
            const typeLabel = item.type || 'assignment';
            const shortDue = formatShortDate(item.dueDate);

            return `
                <tr data-id="${item.id}" style="${isCompleted ? 'opacity: 0.65;' : ''}">
                    <td>
                        <span class="status-pill ${statusClass}">
                            ${relStatus}
                        </span>
                    </td>
                    <td class="fw-semibold" style="color: var(--text-secondary);">
                        ${shortDue}
                    </td>
                    <td>
                        <div class="fw-bold ${isCompleted ? 'text-decoration-line-through text-muted' : ''}" style="color: var(--text-primary);">${escapeHtml(item.title)}</div>
                        ${item.description ? `<div class="small text-muted text-truncate" style="max-width: 280px;">${escapeHtml(item.description)}</div>` : ''}
                    </td>
                    <td class="small fw-semibold" style="color: var(--text-secondary);">
                        ${escapeHtml(typeLabel)}
                    </td>
                    <td>
                        <div class="d-flex align-items-center">
                            <span class="course-dot" style="background-color: ${courseColor};"></span>
                            <span class="fw-bold small" style="color: var(--text-primary);">${escapeHtml(item.subjectCode || 'GEN 101')}</span>
                        </div>
                    </td>
                    <td class="text-end">
                        <div class="d-flex align-items-center justify-content-end gap-3">
                            <button class="btn btn-link btn-sm text-muted p-0 delete-assignment-btn" data-id="${item.id}" title="Delete">
                                <i class="bi bi-trash fs-6"></i>
                            </button>
                            <input class="form-check-input mt-0 toggle-completed-checkbox" type="checkbox"
                                   data-id="${item.id}" ${isCompleted ? 'checked' : ''} title="Mark as complete">
                        </div>
                    </td>
                </tr>
            `;
        }).join('');

        // Wire Complete Checkboxes
        tbody.querySelectorAll('.toggle-completed-checkbox').forEach(cb => {
            cb.addEventListener('change', async (e) => {
                const id = parseInt(cb.getAttribute('data-id'), 10);
                const isChecked = cb.checked;
                await toggleAssignmentCompleted(id, isChecked);
            });
        });

        // Wire Delete Buttons
        tbody.querySelectorAll('.delete-assignment-btn').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const id = parseInt(btn.getAttribute('data-id'), 10);
                if (confirm('Delete this deliverable from overview?')) {
                    await deleteAssignment(id);
                }
            });
        });
    }

    // Toggle Assignment Status
    async function toggleAssignmentCompleted(id, isChecked) {
        const item = allAssignments.find(a => a.id === id);
        if (!item) return;

        const newStatus = isChecked ? 'Completed' : 'In Progress';
        item.status = newStatus;

        try {
            await ApiClient.put(`/assignments/${id}`, {
                id: id,
                title: item.title,
                semesterId: item.semesterId || 5,
                dueDate: item.dueDate,
                status: newStatus
            });
            ApiClient.showToast(`Assignment marked as ${newStatus}`, 'success');
        } catch (e) {
            // Updated in local array
            ApiClient.showToast(`Updated status: ${newStatus}`, 'info');
        }

        renderTable();
        renderProgressGauge();
    }

    // Delete Assignment
    async function deleteAssignment(id) {
        try {
            await ApiClient.delete(`/assignments/${id}`);
            ApiClient.showToast('Deliverable removed.', 'success');
        } catch (e) {
            ApiClient.showToast('Deliverable removed from view.', 'info');
        }
        allAssignments = allAssignments.filter(a => a.id !== id);
        renderTable();
        renderProgressGauge();
    }

    // 12. Search & Filter Handlers
    const searchInput = document.getElementById('tableSearchInput');
    if (searchInput) {
        searchInput.addEventListener('input', () => {
            renderTable();
        });
    }

    const showCompletedCheckbox = document.getElementById('showCompletedCheckbox');
    if (showCompletedCheckbox) {
        showCompletedCheckbox.addEventListener('change', (e) => {
            showCompleted = e.target.checked;
            renderTable();
        });
    }

    const toggleExamsOnlyBtn = document.getElementById('toggleExamsOnlyBtn');
    if (toggleExamsOnlyBtn) {
        toggleExamsOnlyBtn.addEventListener('click', () => {
            isExamsOnly = !isExamsOnly;
            if (isExamsOnly) {
                toggleExamsOnlyBtn.classList.remove('btn-outline-secondary');
                toggleExamsOnlyBtn.classList.add('btn-primary');
            } else {
                toggleExamsOnlyBtn.classList.remove('btn-primary');
                toggleExamsOnlyBtn.classList.add('btn-outline-secondary');
            }
            renderTable();
        });
    }

    // Dropdown filters
    document.querySelectorAll('.filter-opt').forEach(opt => {
        opt.addEventListener('click', (e) => {
            e.preventDefault();
            activeFilterStatus = opt.getAttribute('data-status') || 'ALL';
            renderTable();
            ApiClient.showToast(`Filter applied: ${activeFilterStatus}`, 'info');
        });
    });

    // 13. Quick Add Assignment Modal Form Submission
    const quickAddForm = document.getElementById('quickAddAssignmentForm');
    if (quickAddForm) {
        quickAddForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const title = document.getElementById('quickTitle').value.trim();
            const subjectId = parseInt(document.getElementById('quickCourseSelect').value, 10);
            const type = document.getElementById('quickTypeSelect').value;
            const dueDateVal = document.getElementById('quickDueDate').value;
            const priority = document.getElementById('quickPrioritySelect').value;
            const description = document.getElementById('quickDescription').value.trim();

            if (!title || !dueDateVal) {
                ApiClient.showToast('Please provide a title and due date.', 'warning');
                return;
            }

            // Subject label lookup
            const courseSelect = document.getElementById('quickCourseSelect');
            const subjectText = courseSelect.options[courseSelect.selectedIndex]?.text || 'CS 501';
            const subjectCode = subjectText.split(':')[0].trim();

            const payload = {
                title: title,
                semesterId: 5, // Active Semester 5
                subjectId: subjectId,
                dueDate: new Date(dueDateVal).toISOString(),
                priority: priority,
                status: 'Pending',
                description: description || `Type: ${type}`
            };

            try {
                const created = await ApiClient.post('/assignments', payload);
                created.subjectCode = subjectCode;
                created.type = type;
                allAssignments.unshift(created);
                ApiClient.showToast('New deliverable added to overview!', 'success');
            } catch (err) {
                // Fallback local creation
                const localCreated = {
                    id: Date.now(),
                    title: title,
                    semesterId: 5,
                    subjectId: subjectId,
                    subjectCode: subjectCode,
                    type: type,
                    dueDate: dueDateVal,
                    priority: priority,
                    status: 'Pending',
                    description: description
                };
                allAssignments.unshift(localCreated);
                ApiClient.showToast('Deliverable recorded locally!', 'success');
            }

            // Reset form & close modal
            quickAddForm.reset();
            const modalEl = document.getElementById('newAssignmentModal');
            if (modalEl && window.bootstrap) {
                const modal = bootstrap.Modal.getInstance(modalEl);
                if (modal) modal.hide();
            }

            renderTable();
            renderProgressGauge();
        });
    }

    // 14. Charts Initialization
    function renderCharts(data) {
        const labels = data.semesterLabels || ['Sem 1', 'Sem 2', 'Sem 3', 'Sem 4', 'Sem 5'];
        const sgpaPoints = data.sgpaTrend || [8.65, 8.90, 9.15, 9.30, 9.45];

        // 1. SGPA Progression
        const sgpaCtx = document.getElementById('sgpaTrendChart');
        if (sgpaCtx) {
            if (sgpaChartInstance) sgpaChartInstance.destroy();
            sgpaChartInstance = new Chart(sgpaCtx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{
                        label: 'SGPA (10-point scale)',
                        data: sgpaPoints,
                        borderColor: '#3b82f6',
                        backgroundColor: 'rgba(59, 130, 246, 0.12)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.35,
                        pointBackgroundColor: '#3b82f6',
                        pointBorderColor: '#ffffff',
                        pointBorderWidth: 2,
                        pointRadius: 6,
                        pointHoverRadius: 8
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            min: 6,
                            max: 10,
                            grid: { color: 'rgba(150, 150, 150, 0.1)' }
                        },
                        x: {
                            grid: { display: false }
                        }
                    }
                }
            });
        }

        // 2. Task Status Distribution Doughnut Chart
        const statusCtx = document.getElementById('assignmentStatusChart');
        if (statusCtx) {
            const stats = data.assignmentStats || {};
            const pending = stats['Pending'] || 3;
            const inProgress = stats['In Progress'] || 2;
            const submitted = stats['Submitted'] || 1;
            const completed = stats['Completed'] || 4;

            if (statusDoughnutInstance) statusDoughnutInstance.destroy();
            statusDoughnutInstance = new Chart(statusCtx, {
                type: 'doughnut',
                data: {
                    labels: ['Pending', 'In Progress', 'Submitted', 'Completed'],
                    datasets: [{
                        data: [pending, inProgress, submitted, completed],
                        backgroundColor: ['#f59e0b', '#3b82f6', '#8b5cf6', '#10b981'],
                        borderWidth: 2,
                        borderColor: '#ffffff',
                        hoverOffset: 6
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: { padding: 14, font: { size: 12, weight: '600' } }
                        }
                    },
                    cutout: '70%'
                }
            });
        }
    }

    function useMockDashboardData() {
        const mock = {
            currentSemester: 'Semester 5',
            cgpa: 9.00,
            degreeAudit: {
                totalRequiredCredits: 160,
                earnedCredits: 115,
                inProgressCredits: 18,
                completionPercent: 72
            },
            courseProgress: [
                { subjectCode: 'PHYS 211', progressPercent: 33, dueThisWeek: 1, completedCount: 1, totalCount: 3, color: '#8b5cf6' },
                { subjectCode: 'TE 200', progressPercent: 86, dueThisWeek: 2, completedCount: 18, totalCount: 21, color: '#3b82f6' },
                { subjectCode: 'MATH 241', progressPercent: 0, dueThisWeek: 0, completedCount: 0, totalCount: 5, color: '#ec4899' }
            ],
            upcomingExams: [
                { type: 'Final', subjectCode: 'MATH 241', date: 'Jan 6' },
                { type: 'Exam 1', subjectCode: 'PHYS 211', date: 'Jan 13' }
            ],
            fourYearRoadmap: [
                { yearNumber: 1, yearTitle: '1st Year (Freshman)', semestersText: 'Sem 1 & 2', status: 'COMPLETED', badgeColor: 'success', credits: 46 },
                { yearNumber: 2, yearTitle: '2nd Year (Sophomore)', semestersText: 'Sem 3 & 4', status: 'COMPLETED', badgeColor: 'success', credits: 46 },
                { yearNumber: 3, yearTitle: '3rd Year (Junior)', semestersText: 'Sem 5 & 6', status: 'IN PROGRESS', badgeColor: 'primary', credits: 36 },
                { yearNumber: 4, yearTitle: '4th Year (Senior)', semestersText: 'Sem 7 & 8', status: 'UPCOMING', badgeColor: 'secondary', credits: 32 }
            ],
            semesterLabels: ['Sem 1', 'Sem 2', 'Sem 3', 'Sem 4', 'Sem 5'],
            sgpaTrend: [8.65, 8.90, 9.15, 9.30, 9.45],
            assignmentStats: { 'Pending': 3, 'In Progress': 2, 'Submitted': 1, 'Completed': 4 }
        };

        render4YearRoadmap(mock.fourYearRoadmap, mock.degreeAudit);
        renderCourseProgress(mock.courseProgress);
        renderUpcomingExams(mock.upcomingExams);
        renderProgressGauge();
        renderCharts(mock);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str).replace(/[&<>"']/g, function(m) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
        });
    }
});
