/**
 * CampusTrack — Projects, Internships, Hackathons & Activities
 */
document.addEventListener('DOMContentLoaded', async () => {
    Auth.requireAuth();
    Auth.initNavbar();

    let activitiesList = [];
    let semesters = [];

    const activitiesContainer = document.getElementById('activitiesContainer');
    const categoryTabs = document.querySelectorAll('.act-tab');
    const semesterFilter = document.getElementById('actSemesterFilter');
    const searchInput = document.getElementById('actSearchInput');
    const addActivityForm = document.getElementById('addActivityForm');

    let activeCategory = 'ALL';

    async function init() {
        await Promise.all([loadSemesters(), loadActivities()]);
        setupListeners();
    }

    async function loadSemesters() {
        try {
            semesters = await ApiClient.get('/semesters');
            if (semesterFilter) {
                semesterFilter.innerHTML = `<option value="ALL">All Semesters</option>` +
                    semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
            }
            const modalSem = document.getElementById('actSemesterId');
            if (modalSem) {
                modalSem.innerHTML = `<option value="">General / None</option>` +
                    semesters.map(s => `<option value="${s.id}">Semester ${s.semesterNumber}</option>`).join('');
            }
        } catch (e) {
            console.error('Failed to load semesters:', e);
        }
    }

    async function loadActivities() {
        activitiesContainer.innerHTML = `<div class="col-12 text-center py-5"><div class="spinner-border text-primary"></div></div>`;
        try {
            const sem = semesterFilter ? semesterFilter.value : 'ALL';
            const search = searchInput ? searchInput.value.trim() : '';

            const params = new URLSearchParams();
            if (activeCategory !== 'ALL') params.append('category', activeCategory);
            if (sem !== 'ALL') params.append('semesterId', sem);
            if (search) params.append('search', search);

            const endpoint = `/activities${params.toString() ? '?' + params.toString() : ''}`;
            activitiesList = await ApiClient.get(endpoint);

            renderActivities(activitiesList);
        } catch (e) {
            activitiesContainer.innerHTML = `<div class="col-12 text-center py-5 text-danger">Failed to load activities.</div>`;
        }
    }

    function renderActivities(list) {
        if (!list || list.length === 0) {
            activitiesContainer.innerHTML = `
                <div class="col-12 text-center py-5 text-muted">
                    <i class="bi bi-rocket-takeoff fs-1 text-secondary d-block mb-3"></i>
                    <h5>No projects or activities found</h5>
                    <p class="small">Add your software projects, internships, or hackathon wins using "+ Add Project / Activity".</p>
                </div>
            `;
            return;
        }

        activitiesContainer.innerHTML = list.map(item => {
            const icon = getActivityIcon(item.category);
            const skillsArr = item.skills ? item.skills.split(',').map(s => s.trim()).filter(Boolean) : [];

            return `
                <div class="col-lg-6 col-xl-4 mb-4">
                    <div class="card-glass p-4 h-100 d-flex flex-column justify-content-between">
                        <div>
                            <div class="d-flex justify-content-between align-items-start mb-3">
                                <span class="badge bg-primary-subtle text-primary border border-primary text-uppercase">${item.category}</span>
                                ${item.semesterNumber ? `<span class="badge bg-secondary-subtle text-muted">Sem ${item.semesterNumber}</span>` : ''}
                            </div>
                            <h5 class="text-white fw-bold mb-1">${escapeHtml(item.title)}</h5>
                            <p class="text-info small fw-medium mb-2">
                                <i class="bi ${icon} me-1"></i>${escapeHtml(item.roleOrPosition || 'Contributor')} &bull; ${escapeHtml(item.organization || 'Independent')}
                            </p>
                            <p class="text-muted small mb-3">${escapeHtml(item.description || 'College milestone project.')}</p>
                            
                            ${skillsArr.length > 0 ? `
                                <div class="d-flex flex-wrap gap-1 mb-3">
                                    ${skillsArr.map(sk => `<span class="badge bg-dark border border-secondary border-opacity-50 text-light fw-normal">${escapeHtml(sk)}</span>`).join('')}
                                </div>
                            ` : ''}
                        </div>
                        <div>
                            <div class="d-flex flex-wrap gap-2 pt-2 border-top border-secondary border-opacity-25 justify-content-between align-items-center">
                                <div class="d-flex gap-2">
                                    ${item.githubUrl ? `
                                        <a href="${escapeHtml(item.githubUrl)}" target="_blank" class="btn btn-sm btn-outline-light">
                                            <i class="bi bi-github me-1"></i> GitHub
                                        </a>
                                    ` : ''}
                                    ${item.demoUrl ? `
                                        <a href="${escapeHtml(item.demoUrl)}" target="_blank" class="btn btn-sm btn-outline-info">
                                            <i class="bi bi-box-arrow-up-right me-1"></i> Live Demo
                                        </a>
                                    ` : ''}
                                </div>
                                <button class="btn btn-sm btn-outline-danger delete-act-btn" data-id="${item.id}">
                                    <i class="bi bi-trash"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        document.querySelectorAll('.delete-act-btn').forEach(btn => {
            btn.addEventListener('click', async () => {
                if (confirm('Delete this project/activity?')) {
                    try {
                        await ApiClient.delete(`/activities/${btn.dataset.id}`);
                        ApiClient.showToast('Activity deleted', 'success');
                        loadActivities();
                    } catch (e) {
                        ApiClient.showToast('Failed to delete activity', 'danger');
                    }
                }
            });
        });
    }

    function getActivityIcon(cat) {
        switch (cat) {
            case 'project': return 'bi-code-slash';
            case 'internship': return 'bi-briefcase-fill';
            case 'hackathon': return 'bi-trophy-fill';
            case 'workshop': return 'bi-laptop-fill';
            case 'seminar': return 'bi-mic-fill';
            default: return 'bi-star-fill';
        }
    }

    function setupListeners() {
        categoryTabs.forEach(tab => {
            tab.addEventListener('click', (e) => {
                e.preventDefault();
                categoryTabs.forEach(t => t.classList.remove('active'));
                tab.classList.add('active');
                activeCategory = tab.dataset.category;
                loadActivities();
            });
        });

        if (semesterFilter) semesterFilter.addEventListener('change', loadActivities);
        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(loadActivities, 350);
            });
        }
    }

    if (addActivityForm) {
        addActivityForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const title = document.getElementById('actTitle').value.trim();
            const cat = document.getElementById('actCategory').value;
            const role = document.getElementById('actRole').value.trim();
            const org = document.getElementById('actOrganization').value.trim();
            const semVal = document.getElementById('actSemesterId').value;
            const github = document.getElementById('actGithub').value.trim();
            const demo = document.getElementById('actDemo').value.trim();
            const skills = document.getElementById('actSkills').value.trim();
            const desc = document.getElementById('actDescription').value.trim();

            try {
                await ApiClient.post('/activities', {
                    title: title,
                    category: cat,
                    roleOrPosition: role || null,
                    organization: org || null,
                    semesterId: semVal ? parseInt(semVal) : null,
                    githubUrl: github || null,
                    demoUrl: demo || null,
                    skills: skills || null,
                    description: desc || null
                });
                ApiClient.showToast('Activity record created!', 'success');
                bootstrap.Modal.getInstance(document.getElementById('addActivityModal')).hide();
                addActivityForm.reset();
                loadActivities();
            } catch (err) {
                ApiClient.showToast(err.message || 'Error creating activity', 'danger');
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
