/**
 * CampusTrack — Certificates, Documents & Lab Records Repository
 */
document.addEventListener('DOMContentLoaded', async () => {
    Auth.requireAuth();
    Auth.initNavbar();

    let recordsList = [];
    let semesters = [];

    const recordsContainer = document.getElementById('recordsContainer');
    const categoryTabs = document.querySelectorAll('.category-tab');
    const semesterFilter = document.getElementById('recordSemesterFilter');
    const searchInput = document.getElementById('recordSearchInput');
    const addRecordForm = document.getElementById('addRecordForm');

    let activeCategory = 'ALL';

    async function init() {
        await Promise.all([loadSemesters(), loadRecords()]);
        setupListeners();
    }

    async function loadSemesters() {
        try {
            semesters = await ApiClient.get('/semesters');
            if (semesterFilter) {
                semesterFilter.innerHTML = `<option value="ALL">All Semesters</option>` +
                    semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
            }
            const modalSem = document.getElementById('recordSemesterId');
            if (modalSem) {
                modalSem.innerHTML = `<option value="">General / None</option>` +
                    semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
            }
        } catch (e) {
            console.error('Failed to load semesters:', e);
        }
    }

    async function loadRecords() {
        recordsContainer.innerHTML = `<div class="col-12 text-center py-5"><div class="spinner-border text-info"></div></div>`;
        try {
            const sem = semesterFilter ? semesterFilter.value : 'ALL';
            const search = searchInput ? searchInput.value.trim() : '';

            const params = new URLSearchParams();
            if (activeCategory !== 'ALL') params.append('category', activeCategory);
            if (sem !== 'ALL') params.append('semesterId', sem);
            if (search) params.append('search', search);

            const endpoint = `/records${params.toString() ? '?' + params.toString() : ''}`;
            recordsList = await ApiClient.get(endpoint);

            renderRecords(recordsList);
        } catch (e) {
            recordsContainer.innerHTML = `<div class="col-12 text-center py-5 text-danger">Failed to load documents.</div>`;
        }
    }

    function renderRecords(list) {
        if (!list || list.length === 0) {
            recordsContainer.innerHTML = `
                <div class="col-12 text-center py-5 text-muted">
                    <i class="bi bi-file-earmark-lock2 fs-1 text-secondary d-block mb-3"></i>
                    <h5>No documents found</h5>
                    <p class="small">Upload verified credentials, certificates, or transcripts using "+ Upload Document".</p>
                </div>
            `;
            return;
        }

        recordsContainer.innerHTML = list.map(item => {
            const icon = getCategoryIcon(item.category);
            const dateStr = item.issueDate ? new Date(item.issueDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : 'N/A';

            return `
                <div class="col-lg-6 col-xl-4 mb-4">
                    <div class="card-glass p-4 h-100 d-flex flex-column justify-content-between">
                        <div>
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <div class="stat-icon bg-opacity-10 ${getCategoryBg(item.category)} text-${getCategoryText(item.category)} mb-0">
                                    <i class="bi ${icon}"></i>
                                </div>
                                <span class="badge bg-secondary-subtle text-light border border-secondary text-uppercase">${formatCategory(item.category)}</span>
                            </div>
                            <h5 class="text-white fw-bold mb-1">${escapeHtml(item.title)}</h5>
                            <p class="text-info small fw-medium mb-2"><i class="bi bi-building me-1"></i>${escapeHtml(item.issuer || 'Academic Institution')}</p>
                            <p class="text-muted small mb-3">${escapeHtml(item.description || 'Verified student credential document.')}</p>
                        </div>
                        <div>
                            <div class="bg-dark p-2 rounded mb-3 border border-secondary border-opacity-25 small">
                                <div class="text-secondary mb-1"><i class="bi bi-calendar-event me-1"></i>Date: <span class="text-light">${dateStr}</span></div>
                                ${item.credentialId ? `<div class="text-secondary"><i class="bi bi-patch-check me-1"></i>ID: <span class="text-light">${escapeHtml(item.credentialId)}</span></div>` : ''}
                                ${item.semesterNumber ? `<div class="text-secondary"><i class="bi bi-bookmark me-1"></i>Linked to: <span class="text-light">Semester ${item.semesterNumber}</span></div>` : ''}
                            </div>
                            <div class="d-flex justify-content-between align-items-center">
                                <a href="${escapeHtml(item.fileUrl)}" target="_blank" class="btn btn-sm btn-primary-custom">
                                    <i class="bi bi-box-arrow-up-right me-1"></i> View / Download
                                </a>
                                <button class="btn btn-sm btn-outline-danger delete-record-btn" data-id="${item.id}">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        document.querySelectorAll('.delete-record-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (confirm('Delete this credential record?')) {
                    try {
                        await ApiClient.delete(`/records/${btn.dataset.id}`);
                        ApiClient.showToast('Document record deleted', 'success');
                        loadRecords();
                    } catch (e) {
                        ApiClient.showToast('Failed to delete document', 'danger');
                    }
                }
            });
        });
    }

    function getCategoryIcon(cat) {
        switch (cat) {
            case 'certificate': return 'bi-award-fill';
            case 'result_sheet': return 'bi-file-earmark-ruled-fill';
            case 'lab_record': return 'bi-clipboard2-pulse-fill';
            case 'course_completion': return 'bi-mortarboard-fill';
            default: return 'bi-file-earmark-text-fill';
        }
    }

    function getCategoryBg(cat) {
        switch (cat) {
            case 'certificate': return 'bg-warning';
            case 'result_sheet': return 'bg-success';
            case 'lab_record': return 'bg-info';
            default: return 'bg-primary';
        }
    }

    function getCategoryText(cat) {
        switch (cat) {
            case 'certificate': return 'warning';
            case 'result_sheet': return 'success';
            case 'lab_record': return 'info';
            default: return 'primary';
        }
    }

    function formatCategory(cat) {
        return (cat || '').replace('_', ' ');
    }

    function setupListeners() {
        categoryTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                e.preventDefault();
                categoryTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                activeCategory = tab.dataset.category;
                loadRecords();
            });
        });

        if (semesterFilter) semesterFilter.addEventListener('change', loadRecords);
        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(loadRecords, 350);
            });
        }
    }

    // Handle Upload Document Form
    if (addRecordForm) {
        addRecordForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const title = document.getElementById('recordTitle').value.trim();
            const category = document.getElementById('recordCategory').value;
            const issuer = document.getElementById('recordIssuer').value.trim();
            const date = document.getElementById('recordDate').value;
            const credentialId = document.getElementById('recordCredentialId').value.trim();
            const semVal = document.getElementById('recordSemesterId').value;
            const fileUrl = document.getElementById('recordFileUrl').value.trim();
            const desc = document.getElementById('recordDescription').value.trim();

            try {
                await ApiClient.post('/records', {
                    title: title,
                    category: category,
                    issuer: issuer,
                    issueDate: date || null,
                    credentialId: credentialId || null,
                    semesterId: semVal ? parseInt(semVal) : null,
                    fileUrl: fileUrl,
                    fileName: title.toLowerCase().replace(/\s+/g, '-') + '.pdf',
                    description: desc || null
                });
                ApiClient.showToast('Document record saved successfully!', 'success');
                bootstrap.Modal.getInstance(document.getElementById('uploadRecordModal')).hide();
                addRecordForm.reset();
                loadRecords();
            } catch (err) {
                ApiClient.showToast(err.message || 'Error saving document', 'danger');
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
