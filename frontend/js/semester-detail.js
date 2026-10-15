/**
 * CampusTrack — Semester Academic Workspace Controller (Phase 2)
 * Manages Subjects, Continuous Marks & Assessments, and Semester Examination Results.
 * Integrates directly with Java Servlets REST API backed by MySQL.
 */
document.addEventListener('DOMContentLoaded', () => {

    // Global page state
    let currentSemesterId = null;
    let currentSemester = null;
    let semesterSubjects = [];
    let activeSubjectForAssessments = null;
    let activeSubjectForResults = null;
    let currentAssessmentsList = [];
    let pendingDeleteSubjectId = null;
    let pendingDeleteAssessmentId = null;

    // DOM Elements - Page & Container
    const detailLoadingState = document.getElementById('detailLoadingState');
    const detailErrorState = document.getElementById('detailErrorState');
    const detailErrorMessage = document.getElementById('detailErrorMessage');
    const retryDetailBtn = document.getElementById('retryDetailBtn');
    const detailContentContainer = document.getElementById('detailContentContainer');

    // Breadcrumb & Header
    const breadcrumbSemesterName = document.getElementById('breadcrumbSemesterName');
    const detailSemNumberBadge = document.getElementById('detailSemNumberBadge');
    const detailSemesterTitle = document.getElementById('detailSemesterTitle');
    const detailAcademicYear = document.getElementById('detailAcademicYear');
    const detailRecordId = document.getElementById('detailRecordId');
    const detailStudentScopeLabel = document.getElementById('detailStudentScopeLabel');
    const detailStatusBadge = document.getElementById('detailStatusBadge');
    const detailStatusText = document.getElementById('detailStatusText');

    // KPI Summary Elements
    const kpiSubjectCount = document.getElementById('kpiSubjectCount');
    const kpiTotalCreditsSub = document.getElementById('kpiTotalCreditsSub');
    const kpiAssessmentCount = document.getElementById('kpiAssessmentCount');
    const sgpaStatusBadge = document.getElementById('sgpaStatusBadge');
    const kpiSgpaValue = document.getElementById('kpiSgpaValue');
    const kpiSgpaSubtext = document.getElementById('kpiSgpaSubtext');
    const kpiResultStatus = document.getElementById('kpiResultStatus');
    const kpiEarnedCreditsSub = document.getElementById('kpiEarnedCreditsSub');

    // Subjects Section
    const subjectsGrid = document.getElementById('subjectsGrid');
    const subjectsEmptyState = document.getElementById('subjectsEmptyState');

    // API Status Elements
    const apiStatusDot = document.getElementById('apiStatusDot');
    const apiStatusText = document.getElementById('apiStatusText');
    const currentStudentLabel = document.getElementById('currentStudentLabel');

    // Modals Initialization
    const addSubjectModalEl = document.getElementById('addSubjectModal');
    const addSubjectModal = addSubjectModalEl ? new bootstrap.Modal(addSubjectModalEl) : null;
    const addSubjectForm = document.getElementById('addSubjectForm');
    const addSubjectErrorAlert = document.getElementById('addSubjectErrorAlert');
    const addSubjectSubmitBtn = document.getElementById('addSubjectSubmitBtn');
    const addSubjectSpinner = document.getElementById('addSubjectSpinner');

    const editSubjectModalEl = document.getElementById('editSubjectModal');
    const editSubjectModal = editSubjectModalEl ? new bootstrap.Modal(editSubjectModalEl) : null;
    const editSubjectForm = document.getElementById('editSubjectForm');
    const editSubjectErrorAlert = document.getElementById('editSubjectErrorAlert');
    const editSubjectSubmitBtn = document.getElementById('editSubjectSubmitBtn');
    const editSubjectSpinner = document.getElementById('editSubjectSpinner');

    const deleteSubjectConfirmModalEl = document.getElementById('deleteSubjectConfirmModal');
    const deleteSubjectConfirmModal = deleteSubjectConfirmModalEl ? new bootstrap.Modal(deleteSubjectConfirmModalEl) : null;
    const deleteSubjectConfirmHeading = document.getElementById('deleteSubjectConfirmHeading');
    const deleteSubjectConfirmMessage = document.getElementById('deleteSubjectConfirmMessage');
    const confirmDeleteSubjectBtn = document.getElementById('confirmDeleteSubjectBtn');

    // Assessments Modal Elements
    const assessmentsModalEl = document.getElementById('assessmentsModal');
    const assessmentsModal = assessmentsModalEl ? new bootstrap.Modal(assessmentsModalEl) : null;
    const modalSubjectCodePill = document.getElementById('modalSubjectCodePill');
    const modalSubjectTypeBadge = document.getElementById('modalSubjectTypeBadge');
    const modalSubjectCreditsBadge = document.getElementById('modalSubjectCreditsBadge');
    const modalSubjectTitle = document.getElementById('modalSubjectTitle');
    const modalAssessmentsTotalCount = document.getElementById('modalAssessmentsTotalCount');
    const modalAssessmentsMarksScore = document.getElementById('modalAssessmentsMarksScore');
    const modalAssessmentsOverallPct = document.getElementById('modalAssessmentsOverallPct');
    const modalAssessmentsTotalWeight = document.getElementById('modalAssessmentsTotalWeight');
    const targetMarksInput = document.getElementById('targetMarksInput');
    const targetProgressBar = document.getElementById('targetProgressBar');
    const targetCurrentScoreText = document.getElementById('targetCurrentScoreText');
    const targetGoalScoreText = document.getElementById('targetGoalScoreText');
    const modalAssessmentsEmptyBox = document.getElementById('modalAssessmentsEmptyBox');
    const modalAssessmentsTableWrapper = document.getElementById('modalAssessmentsTableWrapper');
    const modalAssessmentsTableBody = document.getElementById('modalAssessmentsTableBody');
    const openAddAssessmentBtn = document.getElementById('openAddAssessmentBtn');
    const modalAddFirstAssessmentBtn = document.getElementById('modalAddFirstAssessmentBtn');

    // Add / Edit Assessment Modal Elements
    const addAssessmentModalEl = document.getElementById('addAssessmentModal');
    const addAssessmentModal = addAssessmentModalEl ? new bootstrap.Modal(addAssessmentModalEl) : null;
    const addAssessmentForm = document.getElementById('addAssessmentForm');
    const addAssessmentErrorAlert = document.getElementById('addAssessmentErrorAlert');
    const addAssessmentSubmitBtn = document.getElementById('addAssessmentSubmitBtn');
    const addAssessmentSpinner = document.getElementById('addAssessmentSpinner');
    const addAssessmentModalLabel = document.getElementById('addAssessmentModalLabel');
    const assessmentFormSubjectId = document.getElementById('assessmentFormSubjectId');
    const assessmentFormAssessmentId = document.getElementById('assessmentFormAssessmentId');
    const assessmentCategorySelect = document.getElementById('assessmentCategorySelect');
    const assessmentNameInput = document.getElementById('assessmentNameInput');
    const assessmentMaxMarks = document.getElementById('assessmentMaxMarks');
    const assessmentObtainedMarks = document.getElementById('assessmentObtainedMarks');
    const assessmentWeightage = document.getElementById('assessmentWeightage');
    const assessmentDateInput = document.getElementById('assessmentDateInput');
    const assessmentRemarksInput = document.getElementById('assessmentRemarksInput');

    // Delete Assessment Confirm Modal Elements
    const deleteAssessmentConfirmModalEl = document.getElementById('deleteAssessmentConfirmModal');
    const deleteAssessmentConfirmModal = deleteAssessmentConfirmModalEl ? new bootstrap.Modal(deleteAssessmentConfirmModalEl) : null;
    const deleteAssessmentHeading = document.getElementById('deleteAssessmentHeading');
    const confirmDeleteAssessmentBtn = document.getElementById('confirmDeleteAssessmentBtn');

    // Subject Result Modal Elements
    const subjectResultModalEl = document.getElementById('subjectResultModal');
    const subjectResultModal = subjectResultModalEl ? new bootstrap.Modal(subjectResultModalEl) : null;
    const resultModalSubjectCode = document.getElementById('resultModalSubjectCode');
    const resultModalSubjectCredits = document.getElementById('resultModalSubjectCredits');
    const resultModalTitle = document.getElementById('resultModalTitle');
    const subjectResultErrorAlert = document.getElementById('subjectResultErrorAlert');

    const manualResultForm = document.getElementById('manualResultForm');
    const manualResultSubjectId = document.getElementById('manualResultSubjectId');
    const manualEndSemMarks = document.getElementById('manualEndSemMarks');
    const manualFinalMarks = document.getElementById('manualFinalMarks');
    const manualFinalGrade = document.getElementById('manualFinalGrade');
    const manualGradePoint = document.getElementById('manualGradePoint');
    const manualResultStatus = document.getElementById('manualResultStatus');
    const manualRemarks = document.getElementById('manualRemarks');
    const saveManualResultBtn = document.getElementById('saveManualResultBtn');
    const saveManualResultSpinner = document.getElementById('saveManualResultSpinner');

    // Mode B Elements
    const calcInternalWeight = document.getElementById('calcInternalWeight');
    const calcEndSemWeight = document.getElementById('calcEndSemWeight');
    const calcEndSemExamScore = document.getElementById('calcEndSemExamScore');
    const computeEstimateBtn = document.getElementById('computeEstimateBtn');
    const calcPreviewInternal = document.getElementById('calcPreviewInternal');
    const calcPreviewExam = document.getElementById('calcPreviewExam');
    const calcPreviewTotal = document.getElementById('calcPreviewTotal');
    const calcPreviewGrade = document.getElementById('calcPreviewGrade');
    const applyCalculatedResultBtn = document.getElementById('applyCalculatedResultBtn');
    const applyCalculatedResultSpinner = document.getElementById('applyCalculatedResultSpinner');

    // =========================================================================
    // 1. INITIALIZATION & ROUTING
    // =========================================================================

    function resolveSemesterId() {
        const urlParams = new URLSearchParams(window.location.search);
        const queryId = urlParams.get('id');
        if (queryId && !isNaN(parseInt(queryId))) {
            return parseInt(queryId);
        }
        const pathMatches = window.location.pathname.match(/\/semesters\/(\d+)/);
        if (pathMatches && pathMatches[1]) {
            return parseInt(pathMatches[1]);
        }
        return null;
    }

    Auth.requireAuth();
    Auth.initNavbar();

    currentSemesterId = resolveSemesterId();

    if (!currentSemesterId) {
        showFatalError('Invalid or missing semester ID in URL. Please navigate from My Semesters.');
        return;
    }

    updateStudentLabelUI();
    checkBackendHealth();
    loadSemesterWorkspace();

    // =========================================================================
    // 2. BACKEND HEALTH CHECK
    // =========================================================================

    function updateStudentLabelUI() {
        const user = ApiClient.getUser();
        const label = user ? (user.name || user.fullName || 'Student') : 'Student';
        if (currentStudentLabel) currentStudentLabel.textContent = label;
        if (detailStudentScopeLabel) detailStudentScopeLabel.textContent = label;
    }

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
    // 3. WORKSPACE DATA LOADING (SEMESTER, SUMMARY, SUBJECTS)
    // =========================================================================

    async function loadSemesterWorkspace() {
        detailLoadingState.classList.remove('d-none');
        detailErrorState.classList.add('d-none');
        detailContentContainer.classList.add('d-none');

        try {
            // Load semester metadata, academic summary, and subjects in parallel
            const [semData, summaryData, subjectsData] = await Promise.all([
                ApiClient.get(`/semesters/${currentSemesterId}`),
                ApiClient.get(`/semesters/${currentSemesterId}/academic-summary`),
                ApiClient.get(`/semesters/${currentSemesterId}/subjects`)
            ]);

            currentSemester = semData;
            semesterSubjects = Array.isArray(subjectsData) ? subjectsData : [];

            renderSemesterHeader(currentSemester);
            renderAcademicSummary(summaryData);
            renderSubjectsList(semesterSubjects);

            detailLoadingState.classList.add('d-none');
            detailContentContainer.classList.remove('d-none');

        } catch (err) {
            console.error('[SemesterDetail] Failed to load workspace:', err);
            showFatalError(err.message || 'Unable to fetch semester workspace from backend.');
        }
    }

    async function refreshAcademicSummaryAndSubjects() {
        try {
            const [summaryData, subjectsData] = await Promise.all([
                ApiClient.get(`/semesters/${currentSemesterId}/academic-summary`),
                ApiClient.get(`/semesters/${currentSemesterId}/subjects`)
            ]);
            semesterSubjects = Array.isArray(subjectsData) ? subjectsData : [];
            renderAcademicSummary(summaryData);
            renderSubjectsList(semesterSubjects);
        } catch (err) {
            console.error('[SemesterDetail] Error refreshing summary:', err);
        }
    }

    function renderSemesterHeader(sem) {
        const semName = sem.semesterName || `Semester ${sem.semesterNumber}`;
        const semNumber = sem.semesterNumber;
        const academicYear = sem.academicYear || 'Academic Year Pending';
        const isCompleted = (sem.status || '').toLowerCase() === 'completed';
        const statusClass = isCompleted ? 'Completed' : 'Active';

        document.title = `${semName} (${academicYear}) — CampusTrack`;

        if (breadcrumbSemesterName) breadcrumbSemesterName.textContent = semName;
        if (detailSemNumberBadge) detailSemNumberBadge.textContent = semNumber;
        if (detailSemesterTitle) detailSemesterTitle.textContent = semName;
        if (detailAcademicYear) detailAcademicYear.innerHTML = `<i class="bi bi-calendar3 text-teal-custom"></i> ${escapeHtml(academicYear)}`;
        if (detailRecordId) detailRecordId.textContent = `Semester ID: #${sem.id || sem.semesterId || currentSemesterId}`;

        if (detailStatusBadge && detailStatusText) {
            detailStatusBadge.className = `badge-sem-status ${statusClass} fs-6 px-3 py-1.5`;
            detailStatusBadge.innerHTML = `
                ${!isCompleted ? '<span class="status-dot-pulse"></span>' : '<i class="bi bi-check-circle-fill"></i>'}
                <span>${statusClass}</span>
            `;
        }
    }

    function renderAcademicSummary(summary) {
        if (!summary) return;

        // KPI 1: Coursework & Credits
        const subjectCount = summary.subjectCount || 0;
        const totalCredits = summary.totalCredits || 0;
        if (kpiSubjectCount) kpiSubjectCount.textContent = subjectCount;
        if (kpiTotalCreditsSub) kpiTotalCreditsSub.textContent = `${totalCredits} Total Credits`;

        // KPI 2: Assessments
        const assessmentCount = summary.assessmentCount || 0;
        if (kpiAssessmentCount) kpiAssessmentCount.textContent = assessmentCount;

        // KPI 3: Semester SGPA
        const hasSgpa = summary.sgpa !== null && summary.sgpa !== undefined;
        const isProvisional = summary.isProvisional !== false;

        if (kpiSgpaValue) {
            kpiSgpaValue.textContent = hasSgpa ? Number(summary.sgpa).toFixed(2) : '—';
        }

        if (sgpaStatusBadge) {
            if (!hasSgpa) {
                sgpaStatusBadge.className = 'badge bg-secondary bg-opacity-25 text-dark small';
                sgpaStatusBadge.textContent = 'Pending';
            } else if (isProvisional) {
                sgpaStatusBadge.className = 'badge bg-warning bg-opacity-25 text-dark small';
                sgpaStatusBadge.textContent = 'Provisional';
            } else {
                sgpaStatusBadge.className = 'badge bg-success bg-opacity-25 text-success small';
                sgpaStatusBadge.textContent = 'Confirmed';
            }
        }

        if (kpiSgpaSubtext) {
            if (!hasSgpa) {
                kpiSgpaSubtext.textContent = 'Awaiting final grades';
            } else if (isProvisional) {
                kpiSgpaSubtext.textContent = `${summary.evaluatedCredits || 0}/${totalCredits} credits evaluated`;
            } else {
                kpiSgpaSubtext.textContent = `All ${totalCredits} credits evaluated`;
            }
        }

        // KPI 4: Result Completion Status
        const resultStatus = summary.resultStatus || 'Awaiting';
        const earnedCredits = summary.earnedCredits || 0;
        if (kpiResultStatus) {
            kpiResultStatus.textContent = resultStatus;
            kpiResultStatus.className = `kpi-value fs-4 mb-1 ${resultStatus === 'Completed' ? 'text-success' : (resultStatus === 'Failed' ? 'text-danger' : 'text-teal-custom')}`;
        }
        if (kpiEarnedCreditsSub) {
            kpiEarnedCreditsSub.textContent = `${earnedCredits} Earned Credits`;
        }
    }

    // =========================================================================
    // 4. SUBJECTS LIST RENDERING & CARDS
    // =========================================================================

    function renderSubjectsList(subjects) {
        if (!subjectsGrid) return;
        subjectsGrid.innerHTML = '';

        if (!subjects || subjects.length === 0) {
            if (subjectsEmptyState) subjectsEmptyState.classList.remove('d-none');
            return;
        }

        if (subjectsEmptyState) subjectsEmptyState.classList.add('d-none');

        subjects.forEach(sub => {
            const cardEl = createSubjectCardElement(sub);
            subjectsGrid.appendChild(cardEl);
        });
    }

    function createSubjectCardElement(sub) {
        const col = document.createElement('div');
        col.className = 'subject-card-column';

        const subjectId = sub.id || sub.subjectId;
        const subName = sub.subjectName || 'Untitled Course';
        const subCode = sub.subjectCode || '—';
        const credits = sub.credits !== undefined ? sub.credits : 3;
        const subType = sub.subjectType || 'Theory';
        const faculty = sub.facultyName ? escapeHtml(sub.facultyName) : null;
        const desc = sub.description ? escapeHtml(sub.description) : null;

        // Marks Summary
        const assessCount = sub.assessmentCount || 0;
        const totalMarks = sub.totalObtainedMarks || 0;
        const maxMarks = sub.totalMaxMarks || 0;
        const overallScorePct = sub.overallScorePercentage || (maxMarks > 0 ? (totalMarks / maxMarks) * 100 : 0);

        // Final Result
        const result = sub.result || null;
        let gradeBadgeHtml = '';
        let resultSummaryHtml = '';

        if (result && result.resultStatus && result.resultStatus !== 'Awaiting Result') {
            const letterGrade = result.finalGrade || '—';
            const gradePoint = result.gradePoint !== null && result.gradePoint !== undefined ? Number(result.gradePoint).toFixed(1) : '—';
            const status = result.resultStatus || 'Pass';
            const source = result.resultSource || 'MANUAL';
            const statusBadgeClass = status === 'Pass' ? 'badge-result-pass' : 'badge-result-fail';

            gradeBadgeHtml = `
                <div class="d-flex align-items-center gap-2">
                    <span class="badge badge-grade-${letterGrade.replace('+', 'Plus')} fs-6 px-2.5 py-1">Grade ${letterGrade}</span>
                    <span class="${statusBadgeClass}">${status}</span>
                </div>
            `;

            resultSummaryHtml = `
                <div class="subject-result-summary mt-2 pt-2 border-top">
                    <div class="d-flex justify-content-between align-items-center small">
                        <span class="text-muted"><i class="bi bi-award me-1"></i> Grade Point: <strong>${gradePoint}</strong></span>
                        <span class="badge bg-light text-secondary border small" title="${source === 'MANUAL' ? 'Manually entered from official result sheet' : 'Estimated from assessment weightages'}">
                            ${source === 'MANUAL' ? 'Official' : 'Calculated'}
                        </span>
                    </div>
                </div>
            `;
        } else {
            gradeBadgeHtml = `
                <span class="badge-result-awaiting">Awaiting Result</span>
            `;
            resultSummaryHtml = `
                <div class="subject-result-summary mt-2 pt-2 border-top">
                    <span class="text-muted small"><i class="bi bi-hourglass-split me-1"></i> Semester result pending</span>
                </div>
            `;
        }

        col.innerHTML = `
            <div class="subject-card h-100" data-subject-id="${subjectId}">
                <!-- Card Header -->
                <div class="subject-card-header d-flex justify-content-between align-items-start gap-2 mb-2">
                    <div class="d-flex flex-wrap align-items-center gap-1.5">
                        <span class="subject-code-pill">${escapeHtml(subCode)}</span>
                        <span class="badge-sub-type ${escapeHtml(subType)}">${escapeHtml(subType)}</span>
                        <span class="badge bg-light text-secondary border">${credits} Credits</span>
                    </div>
                    <div>
                        ${gradeBadgeHtml}
                    </div>
                </div>

                <!-- Subject Title & Faculty -->
                <h5 class="subject-card-title mb-1 text-dark fw-bold">${escapeHtml(subName)}</h5>
                ${faculty ? `<p class="subject-card-faculty mb-1"><i class="bi bi-person text-teal-custom me-1"></i>${faculty}</p>` : ''}
                ${desc ? `<p class="subject-card-description mb-2 text-muted small">${desc}</p>` : ''}

                <!-- Marks & Continuous Assessments Summary -->
                <div class="subject-marks-meter p-2.5 rounded-3 bg-light border mb-2">
                    <div class="d-flex justify-content-between align-items-center small mb-1">
                        <span class="text-muted fw-semibold"><i class="bi bi-clipboard2-check text-teal-custom me-1"></i>Assessments (${assessCount})</span>
                        <span class="fw-bold text-dark">${assessCount > 0 ? `${Number(totalMarks).toFixed(1)} / ${Number(maxMarks).toFixed(1)}` : 'None yet'}</span>
                    </div>
                    <div class="progress" style="height: 6px;">
                        <div class="progress-bar ${overallScorePct >= 75 ? 'bg-teal-custom' : (overallScorePct >= 50 ? 'bg-primary' : 'bg-warning')}" 
                             role="progressbar" style="width: ${Math.min(100, overallScorePct)}%;"></div>
                    </div>
                    <div class="d-flex justify-content-between align-items-center small mt-1 text-muted" style="font-size: 0.75rem;">
                        <span>Continuous Score:</span>
                        <span class="fw-bold text-teal-custom">${assessCount > 0 ? `${Number(overallScorePct).toFixed(1)}%` : '—'}</span>
                    </div>
                </div>

                <!-- Result Status Summary -->
                ${resultSummaryHtml}

                <!-- Action Buttons -->
                <div class="subject-card-actions mt-3 pt-2 border-top d-flex justify-content-between align-items-center gap-2">
                    <button class="btn btn-academic-teal btn-sm flex-grow-1 open-assessments-btn" data-subject-id="${subjectId}">
                        <i class="bi bi-card-checklist me-1"></i> Marks & Assessments
                    </button>
                    <button class="btn btn-outline-secondary btn-sm open-results-btn" data-subject-id="${subjectId}" title="Semester Results & Final Grade">
                        <i class="bi bi-mortarboard"></i>
                    </button>
                    <div class="dropdown">
                        <button class="btn btn-light btn-sm rounded-circle p-1.5" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="bi bi-three-dots-vertical"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 rounded-3">
                            <li>
                                <button class="dropdown-item edit-subject-btn" data-subject-id="${subjectId}">
                                    <i class="bi bi-pencil-square text-primary me-2"></i> Edit Subject
                                </button>
                            </li>
                            <li><hr class="dropdown-divider"></li>
                            <li>
                                <button class="dropdown-item text-danger delete-subject-btn" data-subject-id="${subjectId}">
                                    <i class="bi bi-trash3 text-danger me-2"></i> Delete Subject
                                </button>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>
        `;

        // Wire Event Listeners
        col.querySelector('.open-assessments-btn').addEventListener('click', () => {
            openAssessmentsWorkspace(sub);
        });

        col.querySelector('.open-results-btn').addEventListener('click', () => {
            openResultsModal(sub);
        });

        col.querySelector('.edit-subject-btn').addEventListener('click', () => {
            openEditSubjectModal(sub);
        });

        col.querySelector('.delete-subject-btn').addEventListener('click', () => {
            openDeleteSubjectConfirm(sub);
        });

        return col;
    }

    // =========================================================================
    // 5. SUBJECT CRUD HANDLERS (ADD, EDIT, DELETE)
    // =========================================================================

    // Add Subject
    if (addSubjectForm) {
        addSubjectForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            addSubjectErrorAlert.classList.add('d-none');

            const name = document.getElementById('addSubjectName').value.trim();
            const code = document.getElementById('addSubjectCode').value.trim().toUpperCase();
            const credits = parseInt(document.getElementById('addSubjectCredits').value);
            const type = document.getElementById('addSubjectType').value;
            const faculty = document.getElementById('addFacultyName').value.trim();
            const desc = document.getElementById('addSubjectDescription').value.trim();

            if (!name) {
                showModalError(addSubjectErrorAlert, 'Subject name is required.');
                return;
            }
            if (!code) {
                showModalError(addSubjectErrorAlert, 'Subject code is required.');
                return;
            }
            if (isNaN(credits) || credits < 1 || credits > 10) {
                showModalError(addSubjectErrorAlert, 'Credits must be an integer between 1 and 10.');
                return;
            }

            // Client-side duplicate check within same semester
            const duplicate = semesterSubjects.find(s => (s.subjectCode || '').toUpperCase() === code);
            if (duplicate) {
                showModalError(addSubjectErrorAlert, `Subject code "${code}" already exists in this semester. Subject codes must be unique within the semester.`);
                return;
            }

            setButtonLoading(addSubjectSubmitBtn, addSubjectSpinner, true);

            try {
                const payload = {
                    semesterId: currentSemesterId,
                    subjectName: name,
                    subjectCode: code,
                    credits: credits,
                    subjectType: type,
                    facultyName: faculty || null,
                    description: desc || null
                };

                await ApiClient.post(`/semesters/${currentSemesterId}/subjects`, payload);
                ApiClient.showToast(`Subject "${code} - ${name}" added successfully!`, 'success');

                addSubjectModal.hide();
                addSubjectForm.reset();
                document.getElementById('addSubjectCredits').value = '3';
                document.getElementById('addSubjectType').value = 'Theory';

                await refreshAcademicSummaryAndSubjects();
            } catch (err) {
                console.error('[AddSubject] Error:', err);
                showModalError(addSubjectErrorAlert, err.message || 'Failed to add subject.');
            } finally {
                setButtonLoading(addSubjectSubmitBtn, addSubjectSpinner, false);
            }
        });
    }

    // Open Edit Subject
    function openEditSubjectModal(sub) {
        if (!editSubjectForm) return;
        editSubjectErrorAlert.classList.add('d-none');

        document.getElementById('editSubjectId').value = sub.id || sub.subjectId;
        document.getElementById('editSubjectName').value = sub.subjectName || '';
        document.getElementById('editSubjectCode').value = sub.subjectCode || '';
        document.getElementById('editSubjectCredits').value = sub.credits !== undefined ? sub.credits : 3;
        document.getElementById('editSubjectType').value = sub.subjectType || 'Theory';
        document.getElementById('editFacultyName').value = sub.facultyName || '';
        document.getElementById('editSubjectDescription').value = sub.description || '';

        editSubjectModal.show();
    }

    // Edit Subject Form Submit
    if (editSubjectForm) {
        editSubjectForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            editSubjectErrorAlert.classList.add('d-none');

            const subjectId = parseInt(document.getElementById('editSubjectId').value);
            const name = document.getElementById('editSubjectName').value.trim();
            const code = document.getElementById('editSubjectCode').value.trim().toUpperCase();
            const credits = parseInt(document.getElementById('editSubjectCredits').value);
            const type = document.getElementById('editSubjectType').value;
            const faculty = document.getElementById('editFacultyName').value.trim();
            const desc = document.getElementById('editSubjectDescription').value.trim();

            if (!name) {
                showModalError(editSubjectErrorAlert, 'Subject name is required.');
                return;
            }
            if (!code) {
                showModalError(editSubjectErrorAlert, 'Subject code is required.');
                return;
            }
            if (isNaN(credits) || credits < 1 || credits > 10) {
                showModalError(editSubjectErrorAlert, 'Credits must be between 1 and 10.');
                return;
            }

            // Check duplicate excluding self
            const duplicate = semesterSubjects.find(s => (s.id || s.subjectId) !== subjectId && (s.subjectCode || '').toUpperCase() === code);
            if (duplicate) {
                showModalError(editSubjectErrorAlert, `Subject code "${code}" is already used by another course in this semester.`);
                return;
            }

            setButtonLoading(editSubjectSubmitBtn, editSubjectSpinner, true);

            try {
                const payload = {
                    subjectName: name,
                    subjectCode: code,
                    credits: credits,
                    subjectType: type,
                    facultyName: faculty || null,
                    description: desc || null
                };

                await ApiClient.put(`/subjects/${subjectId}`, payload);
                ApiClient.showToast(`Subject "${code}" updated successfully!`, 'success');

                editSubjectModal.hide();
                await refreshAcademicSummaryAndSubjects();
            } catch (err) {
                console.error('[EditSubject] Error:', err);
                showModalError(editSubjectErrorAlert, err.message || 'Failed to update subject.');
            } finally {
                setButtonLoading(editSubjectSubmitBtn, editSubjectSpinner, false);
            }
        });
    }

    // Delete Subject Confirmation
    function openDeleteSubjectConfirm(sub) {
        pendingDeleteSubjectId = sub.id || sub.subjectId;
        const subCode = sub.subjectCode || 'this subject';
        const assessCount = sub.assessmentCount || 0;

        if (deleteSubjectConfirmHeading) {
            deleteSubjectConfirmHeading.textContent = `Delete ${subCode}?`;
        }

        if (deleteSubjectConfirmMessage) {
            if (assessCount > 0) {
                deleteSubjectConfirmMessage.innerHTML = `
                    <span class="text-danger fw-bold"><i class="bi bi-exclamation-triangle me-1"></i> Warning:</span> 
                    This subject has <strong>${assessCount} continuous assessments</strong> and recorded examination results. Deleting will permanently remove all associated records!
                `;
            } else {
                deleteSubjectConfirmMessage.textContent = `Are you sure you want to delete ${sub.subjectName} (${subCode})?`;
            }
        }

        deleteSubjectConfirmModal.show();
    }

    if (confirmDeleteSubjectBtn) {
        confirmDeleteSubjectBtn.addEventListener('click', async () => {
            if (!pendingDeleteSubjectId) return;

            confirmDeleteSubjectBtn.disabled = true;
            confirmDeleteSubjectBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Deleting...';

            try {
                await ApiClient.delete(`/subjects/${pendingDeleteSubjectId}`);
                ApiClient.showToast('Subject and all associated records deleted successfully.', 'success');
                deleteSubjectConfirmModal.hide();
                await refreshAcademicSummaryAndSubjects();
            } catch (err) {
                console.error('[DeleteSubject] Error:', err);
                ApiClient.showToast(err.message || 'Failed to delete subject.', 'danger');
            } finally {
                confirmDeleteSubjectBtn.disabled = false;
                confirmDeleteSubjectBtn.textContent = 'Delete Subject';
                pendingDeleteSubjectId = null;
            }
        });
    }

    // =========================================================================
    // 6. MARKS & ASSESSMENTS WORKSPACE MODAL
    // =========================================================================

    async function openAssessmentsWorkspace(sub) {
        activeSubjectForAssessments = sub;
        const subjectId = sub.id || sub.subjectId;

        // Populate header pills
        if (modalSubjectCodePill) modalSubjectCodePill.textContent = sub.subjectCode || '—';
        if (modalSubjectTypeBadge) {
            modalSubjectTypeBadge.textContent = sub.subjectType || 'Theory';
            modalSubjectTypeBadge.className = `badge-sub-type ${sub.subjectType || 'Theory'}`;
        }
        if (modalSubjectCreditsBadge) modalSubjectCreditsBadge.textContent = `${sub.credits || 3} Credits`;
        if (modalSubjectTitle) modalSubjectTitle.textContent = `${sub.subjectName || 'Course'} — Assessments`;

        assessmentsModal.show();
        await loadSubjectAssessments(subjectId);
    }

    async function loadSubjectAssessments(subjectId) {
        if (!modalAssessmentsTableBody) return;

        modalAssessmentsTableBody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center py-4 text-muted">
                    <span class="spinner-border spinner-border-sm text-teal-custom me-2"></span> Loading assessments...
                </td>
            </tr>
        `;

        try {
            const data = await ApiClient.get(`/subjects/${subjectId}/assessments`);
            currentAssessmentsList = Array.isArray(data) ? data : [];
            renderAssessmentsTable(currentAssessmentsList);
            updateAssessmentsKPIs(currentAssessmentsList);
        } catch (err) {
            console.error('[LoadAssessments] Error:', err);
            modalAssessmentsTableBody.innerHTML = `
                <tr>
                    <td colspan="8" class="text-center py-4 text-danger">
                        <i class="bi bi-exclamation-triangle me-1"></i> Failed to load assessments: ${escapeHtml(err.message)}
                    </td>
                </tr>
            `;
        }
    }

    function renderAssessmentsTable(assessments) {
        if (!modalAssessmentsTableBody) return;
        modalAssessmentsTableBody.innerHTML = '';

        if (!assessments || assessments.length === 0) {
            if (modalAssessmentsEmptyBox) modalAssessmentsEmptyBox.classList.remove('d-none');
            if (modalAssessmentsTableWrapper) modalAssessmentsTableWrapper.classList.add('d-none');
            return;
        }

        if (modalAssessmentsEmptyBox) modalAssessmentsEmptyBox.classList.add('d-none');
        if (modalAssessmentsTableWrapper) modalAssessmentsTableWrapper.classList.remove('d-none');

        assessments.forEach(a => {
            const tr = document.createElement('tr');
            const aid = a.id || a.assessmentId;
            const category = a.assessmentCategory || a.assessmentType || 'Assignment';
            const name = a.assessmentName || a.title || 'Untitled';
            const maxMarks = a.maximumMarks !== undefined ? Number(a.maximumMarks) : 100;
            const obtainedMarks = a.obtainedMarks !== undefined ? Number(a.obtainedMarks) : 0;
            const pct = maxMarks > 0 ? (obtainedMarks / maxMarks) * 100 : 0;
            const weight = a.weightagePercentage !== null && a.weightagePercentage !== undefined ? `${Number(a.weightagePercentage).toFixed(0)}%` : '—';
            const dateStr = a.assessmentDate || null;
            const remarks = a.remarks ? escapeHtml(a.remarks) : null;

            tr.innerHTML = `
                <td>
                    <span class="badge bg-light text-dark border px-2 py-1 small fw-semibold">${escapeHtml(category)}</span>
                </td>
                <td>
                    <span class="fw-bold text-dark">${escapeHtml(name)}</span>
                </td>
                <td class="text-center fw-semibold text-secondary">
                    ${maxMarks.toFixed(1)}
                </td>
                <td class="text-center fw-bold text-dark">
                    ${obtainedMarks.toFixed(1)}
                </td>
                <td class="text-center">
                    <span class="badge ${pct >= 75 ? 'bg-teal-custom text-white' : (pct >= 50 ? 'bg-primary text-white' : 'bg-warning text-dark')} px-2.5 py-1">
                        ${pct.toFixed(1)}%
                    </span>
                </td>
                <td class="text-center fw-semibold text-purple" style="color: #7c3aed;">
                    ${weight}
                </td>
                <td>
                    <div class="small text-muted">
                        ${dateStr ? `<div><i class="bi bi-calendar-event me-1"></i>${dateStr}</div>` : ''}
                        ${remarks ? `<div class="text-dark fst-italic">${remarks}</div>` : ''}
                        ${!dateStr && !remarks ? '—' : ''}
                    </div>
                </td>
                <td class="text-end">
                    <button class="btn btn-outline-secondary btn-sm p-1 px-2 me-1 edit-assessment-btn" data-aid="${aid}" title="Edit Marks">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-outline-danger btn-sm p-1 px-2 delete-assessment-btn" data-aid="${aid}" title="Delete">
                        <i class="bi bi-trash3"></i>
                    </button>
                </td>
            `;

            // Row action buttons
            tr.querySelector('.edit-assessment-btn').addEventListener('click', () => {
                openEditAssessmentModal(a);
            });

            tr.querySelector('.delete-assessment-btn').addEventListener('click', () => {
                openDeleteAssessmentConfirm(a);
            });

            modalAssessmentsTableBody.appendChild(tr);
        });
    }

    function updateAssessmentsKPIs(assessments) {
        const count = assessments.length;
        let totalObtained = 0;
        let totalMax = 0;
        let totalWeight = 0;

        assessments.forEach(a => {
            totalObtained += Number(a.obtainedMarks || 0);
            totalMax += Number(a.maximumMarks || 0);
            if (a.weightagePercentage !== null && a.weightagePercentage !== undefined) {
                totalWeight += Number(a.weightagePercentage);
            }
        });

        const overallPct = totalMax > 0 ? (totalObtained / totalMax) * 100 : 0;

        if (modalAssessmentsTotalCount) modalAssessmentsTotalCount.textContent = count;
        if (modalAssessmentsMarksScore) modalAssessmentsMarksScore.textContent = `${totalObtained.toFixed(1)} / ${totalMax.toFixed(1)}`;
        if (modalAssessmentsOverallPct) modalAssessmentsOverallPct.textContent = `${overallPct.toFixed(2)}%`;
        if (modalAssessmentsTotalWeight) modalAssessmentsTotalWeight.textContent = `${totalWeight.toFixed(0)}%`;

        // Update Target Progress Meter
        updateTargetProgress(overallPct);
    }

    function updateTargetProgress(currentPct) {
        const targetGoal = parseFloat(targetMarksInput ? targetMarksInput.value : 85) || 85;
        if (targetGoalScoreText) targetGoalScoreText.textContent = `Goal: ${targetGoal}%`;
        if (targetCurrentScoreText) targetCurrentScoreText.textContent = `Current: ${currentPct.toFixed(2)}%`;

        if (targetProgressBar) {
            // Fill progress toward goal (e.g. 80 / 85 * 100)
            const progressRatio = targetGoal > 0 ? Math.min(100, (currentPct / targetGoal) * 100) : 0;
            targetProgressBar.style.width = `${progressRatio}%`;
            targetProgressBar.setAttribute('aria-valuenow', progressRatio);

            if (currentPct >= targetGoal) {
                targetProgressBar.className = 'progress-bar bg-success';
            } else if (currentPct >= targetGoal * 0.8) {
                targetProgressBar.className = 'progress-bar bg-teal-custom';
            } else {
                targetProgressBar.className = 'progress-bar bg-warning';
            }
        }
    }

    if (targetMarksInput) {
        targetMarksInput.addEventListener('input', () => {
            const totalMax = currentAssessmentsList.reduce((acc, a) => acc + Number(a.maximumMarks || 0), 0);
            const totalObtained = currentAssessmentsList.reduce((acc, a) => acc + Number(a.obtainedMarks || 0), 0);
            const overallPct = totalMax > 0 ? (totalObtained / totalMax) * 100 : 0;
            updateTargetProgress(overallPct);
        });
    }

    // =========================================================================
    // 7. ADD / EDIT ASSESSMENT MODAL
    // =========================================================================

    function openAddAssessmentModal() {
        if (!activeSubjectForAssessments) return;
        addAssessmentErrorAlert.classList.add('d-none');
        addAssessmentForm.reset();

        assessmentFormSubjectId.value = activeSubjectForAssessments.id || activeSubjectForAssessments.subjectId;
        assessmentFormAssessmentId.value = '';
        addAssessmentModalLabel.textContent = `Record Assessment — ${activeSubjectForAssessments.subjectCode}`;
        assessmentMaxMarks.value = '50';
        assessmentCategorySelect.value = 'Assignment';

        addAssessmentModal.show();
    }

    if (openAddAssessmentBtn) openAddAssessmentBtn.addEventListener('click', openAddAssessmentModal);
    if (modalAddFirstAssessmentBtn) modalAddFirstAssessmentBtn.addEventListener('click', openAddAssessmentModal);

    function openEditAssessmentModal(a) {
        if (!activeSubjectForAssessments) return;
        addAssessmentErrorAlert.classList.add('d-none');
        addAssessmentForm.reset();

        assessmentFormSubjectId.value = activeSubjectForAssessments.id || activeSubjectForAssessments.subjectId;
        assessmentFormAssessmentId.value = a.id || a.assessmentId;
        addAssessmentModalLabel.textContent = `Edit Assessment — ${a.assessmentName || 'Assessment'}`;

        assessmentCategorySelect.value = a.assessmentCategory || a.assessmentType || 'Assignment';
        assessmentNameInput.value = a.assessmentName || a.title || '';
        assessmentMaxMarks.value = a.maximumMarks;
        assessmentObtainedMarks.value = a.obtainedMarks;
        assessmentWeightage.value = a.weightagePercentage !== null && a.weightagePercentage !== undefined ? a.weightagePercentage : '';
        assessmentDateInput.value = a.assessmentDate || '';
        assessmentRemarksInput.value = a.remarks || '';

        addAssessmentModal.show();
    }

    if (addAssessmentForm) {
        addAssessmentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            addAssessmentErrorAlert.classList.add('d-none');

            const subjectId = parseInt(assessmentFormSubjectId.value);
            const assessmentId = assessmentFormAssessmentId.value ? parseInt(assessmentFormAssessmentId.value) : null;
            const category = assessmentCategorySelect.value;
            const name = assessmentNameInput.value.trim();
            const maxMarks = parseFloat(assessmentMaxMarks.value);
            const obtainedMarks = parseFloat(assessmentObtainedMarks.value);
            const weightVal = assessmentWeightage.value.trim();
            const weightage = weightVal !== '' ? parseFloat(weightVal) : null;
            const assessDate = assessmentDateInput.value || null;
            const remarks = assessmentRemarksInput.value.trim() || null;

            // Strict Validation
            if (!name) {
                showModalError(addAssessmentErrorAlert, 'Assessment name is required.');
                return;
            }
            if (isNaN(maxMarks) || maxMarks <= 0) {
                showModalError(addAssessmentErrorAlert, 'Maximum marks must be greater than zero.');
                return;
            }
            if (isNaN(obtainedMarks) || obtainedMarks < 0) {
                showModalError(addAssessmentErrorAlert, 'Obtained marks cannot be negative.');
                return;
            }
            if (obtainedMarks > maxMarks) {
                showModalError(addAssessmentErrorAlert, `Obtained marks (${obtainedMarks}) cannot exceed maximum marks (${maxMarks}).`);
                return;
            }
            if (weightage !== null && (isNaN(weightage) || weightage < 0 || weightage > 100)) {
                showModalError(addAssessmentErrorAlert, 'Weightage percentage must be between 0 and 100.');
                return;
            }

            setButtonLoading(addAssessmentSubmitBtn, addAssessmentSpinner, true);

            try {
                const payload = {
                    subjectId: subjectId,
                    assessmentName: name,
                    assessmentCategory: category,
                    assessmentType: category,
                    maximumMarks: maxMarks,
                    obtainedMarks: obtainedMarks,
                    weightagePercentage: weightage,
                    assessmentDate: assessDate,
                    remarks: remarks
                };

                if (assessmentId) {
                    await ApiClient.put(`/assessments/${assessmentId}`, payload);
                    ApiClient.showToast('Assessment updated successfully!', 'success');
                } else {
                    await ApiClient.post(`/subjects/${subjectId}/assessments`, payload);
                    ApiClient.showToast('Assessment recorded successfully!', 'success');
                }

                addAssessmentModal.hide();

                // Refresh modal and summary
                await loadSubjectAssessments(subjectId);
                await refreshAcademicSummaryAndSubjects();

            } catch (err) {
                console.error('[SaveAssessment] Error:', err);
                showModalError(addAssessmentErrorAlert, err.message || 'Failed to save assessment.');
            } finally {
                setButtonLoading(addAssessmentSubmitBtn, addAssessmentSpinner, false);
            }
        });
    }

    // Delete Assessment
    function openDeleteAssessmentConfirm(a) {
        pendingDeleteAssessmentId = a.id || a.assessmentId;
        if (deleteAssessmentHeading) {
            deleteAssessmentHeading.textContent = `Delete ${a.assessmentName || 'Assessment'}?`;
        }
        deleteAssessmentConfirmModal.show();
    }

    if (confirmDeleteAssessmentBtn) {
        confirmDeleteAssessmentBtn.addEventListener('click', async () => {
            if (!pendingDeleteAssessmentId) return;

            confirmDeleteAssessmentBtn.disabled = true;
            confirmDeleteAssessmentBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Deleting...';

            try {
                await ApiClient.delete(`/assessments/${pendingDeleteAssessmentId}`);
                ApiClient.showToast('Assessment deleted successfully.', 'success');
                deleteAssessmentConfirmModal.hide();

                if (activeSubjectForAssessments) {
                    const sid = activeSubjectForAssessments.id || activeSubjectForAssessments.subjectId;
                    await loadSubjectAssessments(sid);
                }
                await refreshAcademicSummaryAndSubjects();
            } catch (err) {
                console.error('[DeleteAssessment] Error:', err);
                ApiClient.showToast(err.message || 'Failed to delete assessment.', 'danger');
            } finally {
                confirmDeleteAssessmentBtn.disabled = false;
                confirmDeleteAssessmentBtn.textContent = 'Delete';
                pendingDeleteAssessmentId = null;
            }
        });
    }

    // =========================================================================
    // 8. SEMESTER RESULT MODAL (DUAL MODE: MODE A MANUAL VS MODE B CALCULATED)
    // =========================================================================

    async function openResultsModal(sub) {
        activeSubjectForResults = sub;
        const subjectId = sub.id || sub.subjectId;

        if (resultModalSubjectCode) resultModalSubjectCode.textContent = sub.subjectCode || '—';
        if (resultModalSubjectCredits) resultModalSubjectCredits.textContent = `${sub.credits || 3} Credits`;
        if (resultModalTitle) resultModalTitle.textContent = `${sub.subjectName || 'Course'} — Final Result`;
        subjectResultErrorAlert.classList.add('d-none');

        manualResultSubjectId.value = subjectId;
        manualResultForm.reset();

        // Switch to Mode A tab by default
        const manualTabBtn = document.getElementById('manualModeTab');
        if (manualTabBtn) {
            const tab = bootstrap.Tab.getOrCreateInstance(manualTabBtn);
            tab.show();
        }

        subjectResultModal.show();

        // Fetch current saved result from backend
        try {
            const existingResult = await ApiClient.get(`/subjects/${subjectId}/result`);
            if (existingResult) {
                activeSubjectForResults.result = existingResult;
                populateResultModalFields(existingResult);
            }
        } catch (err) {
            console.error('[GetResult] Error:', err);
        }

        // Initialize Mode B calculation preview
        computeEstimate();
    }

    function populateResultModalFields(res) {
        if (!res) return;
        if (res.endSemesterMarks !== undefined && res.endSemesterMarks !== null) {
            manualEndSemMarks.value = res.endSemesterMarks;
        }
        if (res.finalMarks !== undefined && res.finalMarks !== null) {
            manualFinalMarks.value = res.finalMarks;
        }
        if (res.finalGrade) {
            manualFinalGrade.value = res.finalGrade;
        }
        if (res.gradePoint !== undefined && res.gradePoint !== null) {
            manualGradePoint.value = res.gradePoint;
        }
        if (res.resultStatus) {
            manualResultStatus.value = res.resultStatus;
        }
        if (res.remarks) {
            manualRemarks.value = res.remarks;
        }
    }

    // Auto-fill grade point on letter grade selection
    if (manualFinalGrade) {
        manualFinalGrade.addEventListener('change', () => {
            const gradePointsMap = {
                'O': 10.0,
                'A+': 9.0,
                'A': 8.0,
                'B+': 7.0,
                'B': 6.0,
                'C': 5.0,
                'P': 4.0,
                'RA': 0.0
            };
            const selected = manualFinalGrade.value;
            if (gradePointsMap[selected] !== undefined && !manualGradePoint.value) {
                manualGradePoint.value = gradePointsMap[selected].toFixed(1);
            }
            if (selected === 'RA') {
                manualResultStatus.value = 'Fail';
            } else if (selected && selected !== '') {
                manualResultStatus.value = 'Pass';
            }
        });
    }

    // Save Mode A: Manual Official Entry
    if (manualResultForm) {
        manualResultForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            subjectResultErrorAlert.classList.add('d-none');

            const subjectId = parseInt(manualResultSubjectId.value);
            const endMarksVal = manualEndSemMarks.value.trim();
            const finalMarksVal = manualFinalMarks.value.trim();
            const grade = manualFinalGrade.value;
            const gpVal = manualGradePoint.value.trim();
            const status = manualResultStatus.value;
            const remarks = manualRemarks.value.trim();

            if (!grade) {
                showModalError(subjectResultErrorAlert, 'Please select a final letter grade.');
                return;
            }
            if (!gpVal || isNaN(parseFloat(gpVal))) {
                showModalError(subjectResultErrorAlert, 'Please enter a valid grade point between 0.0 and 10.0.');
                return;
            }
            const gradePoint = parseFloat(gpVal);
            if (gradePoint < 0 || gradePoint > 10) {
                showModalError(subjectResultErrorAlert, 'Grade point must be between 0.0 and 10.0.');
                return;
            }

            setButtonLoading(saveManualResultBtn, saveManualResultSpinner, true);

            try {
                const payload = {
                    subjectId: subjectId,
                    endSemesterMarks: endMarksVal !== '' ? parseFloat(endMarksVal) : null,
                    examinationMaximumMarks: 100.0,
                    finalMarks: finalMarksVal !== '' ? parseFloat(finalMarksVal) : null,
                    finalGrade: grade,
                    gradePoint: gradePoint,
                    resultStatus: status,
                    resultSource: 'MANUAL',
                    remarks: remarks || 'Official result entered manually'
                };

                await ApiClient.put(`/subjects/${subjectId}/result`, payload);
                ApiClient.showToast('Official examination result saved successfully!', 'success');

                subjectResultModal.hide();
                await refreshAcademicSummaryAndSubjects();
            } catch (err) {
                console.error('[SaveResult] Error:', err);
                showModalError(subjectResultErrorAlert, err.message || 'Failed to save subject result.');
            } finally {
                setButtonLoading(saveManualResultBtn, saveManualResultSpinner, false);
            }
        });
    }

    // Compute Mode B: Calculated Estimate
    function computeEstimate() {
        if (!activeSubjectForResults) return null;

        const internalWeightPct = parseFloat(calcInternalWeight ? calcInternalWeight.value : 40) || 40;
        const endSemWeightPct = parseFloat(calcEndSemWeight ? calcEndSemWeight.value : 60) || 60;
        const examScore = parseFloat(calcEndSemExamScore ? calcEndSemExamScore.value : 0) || 0;

        // Continuous Assessment Internal Score
        let internalObtained = 0;
        let internalMax = 0;

        if (activeSubjectForResults.totalMaxMarks && activeSubjectForResults.totalMaxMarks > 0) {
            internalObtained = Number(activeSubjectForResults.totalObtainedMarks || 0);
            internalMax = Number(activeSubjectForResults.totalMaxMarks);
        } else if (currentAssessmentsList && currentAssessmentsList.length > 0) {
            currentAssessmentsList.forEach(a => {
                internalObtained += Number(a.obtainedMarks || 0);
                internalMax += Number(a.maximumMarks || 0);
            });
        }

        const internalScorePct = internalMax > 0 ? (internalObtained / internalMax) * 100 : 0;
        const internalContribution = (internalScorePct * internalWeightPct) / 100;
        const examContribution = (Math.min(100, Math.max(0, examScore)) * endSemWeightPct) / 100;
        const totalMarks = Math.round((internalContribution + examContribution) * 10) / 10;

        // Suggested Grade & Grade Point
        let suggestedGrade = 'RA';
        let suggestedGP = 0.0;
        let suggestedStatus = 'Fail';

        if (totalMarks >= 90) { suggestedGrade = 'O'; suggestedGP = 10.0; suggestedStatus = 'Pass'; }
        else if (totalMarks >= 80) { suggestedGrade = 'A+'; suggestedGP = 9.0; suggestedStatus = 'Pass'; }
        else if (totalMarks >= 70) { suggestedGrade = 'A'; suggestedGP = 8.0; suggestedStatus = 'Pass'; }
        else if (totalMarks >= 60) { suggestedGrade = 'B+'; suggestedGP = 7.0; suggestedStatus = 'Pass'; }
        else if (totalMarks >= 50) { suggestedGrade = 'B'; suggestedGP = 6.0; suggestedStatus = 'Pass'; }
        else if (totalMarks >= 45) { suggestedGrade = 'C'; suggestedGP = 5.0; suggestedStatus = 'Pass'; }
        else if (totalMarks >= 40) { suggestedGrade = 'P'; suggestedGP = 4.0; suggestedStatus = 'Pass'; }

        // Render preview
        if (calcPreviewInternal) calcPreviewInternal.textContent = `${internalContribution.toFixed(1)} / ${internalWeightPct}`;
        if (calcPreviewExam) calcPreviewExam.textContent = `${examContribution.toFixed(1)} / ${endSemWeightPct}`;
        if (calcPreviewTotal) calcPreviewTotal.textContent = totalMarks.toFixed(1);
        if (calcPreviewGrade) {
            calcPreviewGrade.textContent = `${suggestedGrade} (${suggestedGP.toFixed(1)})`;
            calcPreviewGrade.className = `badge badge-grade-${suggestedGrade.replace('+', 'Plus')} fs-6`;
        }

        return {
            internalContribution,
            examContribution,
            totalMarks,
            suggestedGrade,
            suggestedGP,
            suggestedStatus,
            examScore
        };
    }

    if (computeEstimateBtn) {
        computeEstimateBtn.addEventListener('click', computeEstimate);
    }

    if (calcEndSemExamScore) {
        calcEndSemExamScore.addEventListener('input', computeEstimate);
    }

    // Apply Mode B Result
    if (applyCalculatedResultBtn) {
        applyCalculatedResultBtn.addEventListener('click', async () => {
            if (!activeSubjectForResults) return;
            subjectResultErrorAlert.classList.add('d-none');

            const estimate = computeEstimate();
            if (!estimate) return;

            // Check if existing result is MANUAL to prevent silent overwrite
            if (activeSubjectForResults.result && activeSubjectForResults.result.resultSource === 'MANUAL') {
                const confirmed = window.confirm(
                    'Notice: An official manually entered result already exists for this subject. ' +
                    'Applying this calculated estimate will replace the official result. Do you want to proceed?'
                );
                if (!confirmed) return;
            }

            setButtonLoading(applyCalculatedResultBtn, applyCalculatedResultSpinner, true);

            try {
                const subjectId = activeSubjectForResults.id || activeSubjectForResults.subjectId;
                const payload = {
                    subjectId: subjectId,
                    endSemesterMarks: estimate.examScore,
                    examinationMaximumMarks: 100.0,
                    finalMarks: estimate.totalMarks,
                    finalGrade: estimate.suggestedGrade,
                    gradePoint: estimate.suggestedGP,
                    resultStatus: estimate.suggestedStatus,
                    resultSource: 'CALCULATED',
                    remarks: `Calculated estimate (${calcInternalWeight.value}% internal + ${calcEndSemWeight.value}% end-sem)`
                };

                await ApiClient.put(`/subjects/${subjectId}/result`, payload);
                ApiClient.showToast('Calculated result applied successfully!', 'success');

                subjectResultModal.hide();
                await refreshAcademicSummaryAndSubjects();
            } catch (err) {
                console.error('[ApplyCalculatedResult] Error:', err);
                showModalError(subjectResultErrorAlert, err.message || 'Failed to apply calculated result.');
            } finally {
                setButtonLoading(applyCalculatedResultBtn, applyCalculatedResultSpinner, false);
            }
        });
    }

    // =========================================================================
    // 9. HELPER UTILITIES
    // =========================================================================

    function showFatalError(msg) {
        detailLoadingState.classList.add('d-none');
        detailContentContainer.classList.add('d-none');
        detailErrorState.classList.remove('d-none');
        if (detailErrorMessage) detailErrorMessage.textContent = msg;
    }

    if (retryDetailBtn) {
        retryDetailBtn.addEventListener('click', () => {
            loadSemesterWorkspace();
        });
    }

    function showModalError(alertEl, msg) {
        if (!alertEl) return;
        alertEl.textContent = msg;
        alertEl.classList.remove('d-none');
    }

    function setButtonLoading(btn, spinner, isLoading) {
        if (!btn) return;
        btn.disabled = isLoading;
        if (spinner) {
            if (isLoading) spinner.classList.remove('d-none');
            else spinner.classList.add('d-none');
        }
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
