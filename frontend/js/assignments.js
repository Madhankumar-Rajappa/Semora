/**
 * CampusTrack — Assignments & Deadlines Tracker
 */
document.addEventListener('DOMContentLoaded', async () => {
    Auth.requireAuth();
    Auth.initNavbar();

    let assignmentsList = [];
    let semesters = [];
    let subjects = [];

    const assignmentsContainer = document.getElementById('assignmentsContainer');
    const filterStatus = document.getElementById('filterStatus');
    const filterPriority = document.getElementById('filterPriority');
    const filterSemester = document.getElementById('filterSemester');
    const addAssignmentForm = document.getElementById('addAssignmentForm');

    async function init() {
        await Promise.all([loadSemestersAndSubjects(), loadAssignments()]);
        setupFilterListeners();
    }

    async function loadSemestersAndSubjects() {
        try {
            semesters = await ApiClient.get('/semesters');
            subjects = await ApiClient.get('/subjects');

            // Populate semester filter
            if (filterSemester) {
                filterSemester.innerHTML = `<option value="ALL">All Semesters</option>` + 
                    semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
            }

            // Populate modal semester and subject dropdowns
            const modalSem = document.getElementById('assignmentSemesterId');
            if (modalSem) {
                modalSem.innerHTML = semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber} (${s.academicYear})</option>`).join('');
            }

            const modalSub = document.getElementById('assignmentSubjectId');
            if (modalSub) {
                modalSub.innerHTML = `<option value="">General / No Subject</option>` +
                    subjects.map(sub => `<option value="${sub.id}">${sub.subjectCode} — ${sub.subjectName}</option>`).join('');
            }
        } catch (e) {
            console.error('Failed to load filter metadata:', e);
        }
    }

    async function loadAssignments() {
        assignmentsContainer.innerHTML = `<div class="col-12 text-center py-5"><div class="spinner-border text-primary"></div></div>`;
        try {
            const sem = filterSemester ? filterSemester.value : 'ALL';
            const status = filterStatus ? filterStatus.value : 'ALL';
            const priority = filterPriority ? filterPriority.value : 'ALL';

            const queryParams = new URLSearchParams();
            if (sem !== 'ALL') queryParams.append('semesterId', sem);
            if (status !== 'ALL') queryParams.append('status', status);
            if (priority !== 'ALL') queryParams.append('priority', priority);

            const endpoint = `/assignments${queryParams.toString() ? '?' + queryParams.toString() : ''}`;
            assignmentsList = await ApiClient.get(endpoint);

            renderAssignments(assignmentsList);
            updateStats(assignmentsList);
        } catch (e) {
            assignmentsContainer.innerHTML = `<div class="col-12 text-center py-5 text-danger">Failed to load assignments.</div>`;
        }
    }

    function renderAssignments(list) {
        if (!list || list.length === 0) {
            assignmentsContainer.innerHTML = `
                <div class="col-12 text-center py-5 text-muted">
                    <i class="bi bi-clipboard-check fs-1 text-secondary d-block mb-3"></i>
                    <h5>No assignments found matching criteria</h5>
                    <p class="small">Click "+ New Assignment" above to add coursework or tasks.</p>
                </div>
            `;
            return;
        }

        assignmentsContainer.innerHTML = list.map(item => {
            const due = new Date(item.dueDate);
            const formattedDate = due.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' });
            const isCompleted = item.status === 'Completed' || item.status === 'Submitted';
            const overdueBadge = item.isOverdue ? `<span class="badge bg-danger ms-2"><i class="bi bi-exclamation-triangle-fill me-1"></i>OVERDUE</span>` : '';

            return `
                <div class="col-lg-6 col-xl-4 mb-4">
                    <div class="card-glass p-4 h-100 d-flex flex-column justify-content-between">
                        <div>
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <span class="badge badge-priority-${item.priority || 'MEDIUM'}">${item.priority || 'MEDIUM'}</span>
                                <div>
                                    <span class="badge badge-status-${(item.status || 'Pending').replace(' ', '')}">${item.status || 'Pending'}</span>
                                    ${overdueBadge}
                                </div>
                            </div>
                            <h5 class="text-white fw-bold mb-2 ${isCompleted ? 'text-decoration-line-through text-muted' : ''}">
                                ${escapeHtml(item.title)}
                            </h5>
                            <p class="text-muted small mb-3">
                                ${escapeHtml(item.description || 'No additional instructions provided.')}
                            </p>
                        </div>
                        <div>
                            <div class="bg-dark p-2 rounded mb-3 border border-secondary border-opacity-25 small">
                                <div class="text-light mb-1"><i class="bi bi-book me-1 text-info"></i><strong>${escapeHtml(item.subjectCode || 'General')}:</strong> ${escapeHtml(item.subjectName || 'Coursework')}</div>
                                <div class="text-secondary"><i class="bi bi-clock-history me-1 text-warning"></i>Due: <span class="${item.isOverdue ? 'text-danger fw-bold' : 'text-light'}">${formattedDate}</span></div>
                                ${item.attachmentUrl ? `<div class="mt-1"><a href="${escapeHtml(item.attachmentUrl)}" target="_blank" class="text-info text-decoration-none"><i class="bi bi-paperclip me-1"></i>View Attachment</a></div>` : ''}
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <div class="btn-group btn-group-sm">
                                    <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                                        Status: ${item.status}
                                    </button>
                                    <ul class="dropdown-menu dropdown-menu-dark">
                                        <li><a class="dropdown-item change-status" href="#" data-id="${item.id}" data-status="Pending">Pending</a></li>
                                        <li><a class="dropdown-item change-status" href="#" data-id="${item.id}" data-status="In Progress">In Progress</a></li>
                                        <li><a class="dropdown-item change-status" href="#" data-id="${item.id}" data-status="Submitted">Submitted</a></li>
                                        <li><a class="dropdown-item change-status" href="#" data-id="${item.id}" data-status="Completed">Completed</a></li>
                                    </ul>
                                </div>
                                <button class="btn btn-sm btn-outline-danger delete-assignment-btn" data-id="${item.id}">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        // Attach event handlers
        document.querySelectorAll('.change-status').forEach(link => {
            link.addEventListener('click', async (e) => {
                e.preventDefault();
                const id = link.dataset.id;
                const newStatus = link.dataset.status;
                const item = assignmentsList.find(a => a.id == id);
                if (item) {
                    try {
                        item.status = newStatus;
                        await ApiClient.put(`/assignments/${id}`, item);
                        ApiClient.showToast(`Status updated to ${newStatus}`, 'success');
                        loadAssignments();
                    } catch (err) {
                        ApiClient.showToast('Failed to update assignment status', 'danger');
                    }
                }
            });
        });

        document.querySelectorAll('.delete-assignment-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (confirm('Delete this assignment?')) {
                    try {
                        await ApiClient.delete(`/assignments/${btn.dataset.id}`);
                        ApiClient.showToast('Assignment deleted', 'success');
                        loadAssignments();
                    } catch (e) {
                        ApiClient.showToast('Failed to delete assignment', 'danger');
                    }
                }
            });
        });
    }

    function updateStats(list) {
        const total = list.length;
        const pending = list.filter(i => i.status === 'Pending').length;
        const inProgress = list.filter(i => i.status === 'In Progress').length;
        const completed = list.filter(i => i.status === 'Completed' || i.status === 'Submitted').length;
        const overdue = list.filter(i => i.isOverdue).length;

        document.getElementById('statTotalAssignments').textContent = total;
        document.getElementById('statPendingAssignments').textContent = pending + inProgress;
        document.getElementById('statCompletedAssignments').textContent = completed;
        document.getElementById('statOverdueAssignments').textContent = overdue;
    }

    function setupFilterListeners() {
        if (filterStatus) filterStatus.addEventListener('change', loadAssignments);
        if (filterPriority) filterPriority.addEventListener('change', loadAssignments);
        if (filterSemester) filterSemester.addEventListener('change', loadAssignments);
    }

    // Add assignment modal submission
    if (addAssignmentForm) {
        addAssignmentForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const semId = parseInt(document.getElementById('assignmentSemesterId').value);
            const subIdVal = document.getElementById('assignmentSubjectId').value;
            const subId = subIdVal ? parseInt(subIdVal) : null;
            const title = document.getElementById('assignmentTitle').value.trim();
            const desc = document.getElementById('assignmentDescription').value.trim();
            const due = document.getElementById('assignmentDueDate').value;
            const priority = document.getElementById('assignmentPriority').value;
            const status = document.getElementById('assignmentStatus').value;
            const attach = document.getElementById('assignmentAttachment').value.trim();

            try {
                await ApiClient.post('/assignments', {
                    semesterId: semId,
                    subjectId: subId,
                    title: title,
                    description: desc,
                    dueDate: due ? due.replace('T', ' ') + ':00' : null,
                    priority: priority,
                    status: status,
                    attachmentUrl: attach || null
                });
                ApiClient.showToast('Assignment created successfully!', 'success');
                bootstrap.Modal.getInstance(document.getElementById('newAssignmentModal')).hide();
                addAssignmentForm.reset();
                loadAssignments();
            } catch (err) {
                ApiClient.showToast(err.message || 'Error creating assignment', 'danger');
            }
        });
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/[&<>"']/g, function(m) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m];
        });
    }

    init();
});
