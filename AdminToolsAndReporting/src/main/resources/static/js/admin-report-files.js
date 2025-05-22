document.addEventListener('DOMContentLoaded', function () {
    const API_BASE_URL = '/api/admin';
    const generatedReportsList = document.getElementById('generatedReportsList');
    const refreshReportListBtn = document.getElementById('refreshReportListBtn');
    const actionMessageEl = document.getElementById('actionMessageFiles');
    const logsLinkSidebarFiles = document.getElementById('logsLinkSidebarFiles');

    const reportModal = document.getElementById('reportModal');
    const closeModalButton = document.getElementById('closeModalButton');
    const modalReportTitle = document.getElementById('modalReportTitle');
    const modalReportContent = document.getElementById('modalReportContent');

    function showActionMessage(message, isError = false) {
        actionMessageEl.textContent = message;
        actionMessageEl.classList.remove('hidden', 'bg-green-100', 'text-green-700', 'bg-red-100', 'text-red-700');
        if (isError) {
            actionMessageEl.classList.add('bg-red-100', 'text-red-700');
        } else {
            actionMessageEl.classList.add('bg-green-100', 'text-green-700');
        }
        actionMessageEl.classList.remove('hidden');
        setTimeout(() => actionMessageEl.classList.add('hidden'), 4000);
    }

    if (logsLinkSidebarFiles) {
        logsLinkSidebarFiles.addEventListener('click', (e) => {
            e.preventDefault();
            window.open('/api/admin/logs?limit=100', '_blank');
        });
    }

    async function fetchGeneratedReportFiles() {
        try {
            const response = await fetch(`${API_BASE_URL}/reports/list`);
            if (!response.ok) throw new Error('Failed to load generated reports list');
            const reportFiles = await response.json();
            generatedReportsList.innerHTML = '';

            if (reportFiles.length === 0) {
                generatedReportsList.innerHTML = `<li class="text-gray-500 p-3 text-center">No reports generated yet.</li>`;
                return;
            }

            reportFiles.forEach(fileName => {
                const listItem = document.createElement('li');
                listItem.className = 'flex justify-between items-center p-3 border rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors';
                listItem.innerHTML = `
                    <span class="text-gray-700 font-medium">${fileName}</span>
                    <div class="flex space-x-2">
                        <button data-filename="${fileName}" class="view-report-btn text-blue-500 hover:text-blue-700 hover:underline focus:outline-none text-sm py-1 px-2 rounded border border-blue-500 hover:bg-blue-50">
                            <i class="fas fa-eye mr-1"></i>View
                        </button>
                        <a href="${API_BASE_URL}/reports/download/${fileName}" target="_blank" class="text-green-500 hover:text-green-700 hover:underline focus:outline-none text-sm py-1 px-2 rounded border border-green-500 hover:bg-green-50">
                            <i class="fas fa-download mr-1"></i>Download
                        </a>
                    </div>
                `;
                generatedReportsList.appendChild(listItem);
            });

            
            document.querySelectorAll('.view-report-btn').forEach(button => {
                button.addEventListener('click', async (event) => {
                    const fileName = event.currentTarget.dataset.filename;
                    try {
                        const res = await fetch(`${API_BASE_URL}/reports/view/${fileName}`);
                        if(!res.ok) throw new Error(`Failed to fetch report: ${res.statusText}`);
                        const content = await res.text();
                        modalReportTitle.textContent = `Report: ${fileName}`;
                        modalReportContent.textContent = content;
                        reportModal.classList.remove('hidden');
                    } catch(err) {
                        console.error("Error viewing report:", err);
                        showActionMessage("Error viewing report: " + err.message, true);
                    }
                });
            });

        } catch (error) {
            console.error(error);
            generatedReportsList.innerHTML = `<li class="text-red-500 p-3 text-center">${error.message}</li>`;
            showActionMessage(error.message, true);
        }
    }

    if(closeModalButton) {
        closeModalButton.addEventListener('click', () => {
            reportModal.classList.add('hidden');
        });
    }
    
    if(reportModal) {
        reportModal.addEventListener('click', (event) => {
            if (event.target === reportModal) {
                reportModal.classList.add('hidden');
            }
        });
        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape' && !reportModal.classList.contains('hidden')) {
                reportModal.classList.add('hidden');
            }
        });
    }

    if (refreshReportListBtn) {
        refreshReportListBtn.addEventListener('click', () => {
            showActionMessage('Refreshing report list...');
            fetchGeneratedReportFiles();
        });
    }

    
    fetchGeneratedReportFiles();
});
