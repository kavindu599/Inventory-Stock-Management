document.addEventListener('DOMContentLoaded', function () {
    const API_BASE_URL = '/api/admin';

    // Stats elements
    const reportLowStockEl = document.getElementById('reportLowStock');
    const reportExpiringSoonEl = document.getElementById('reportExpiringSoon');
    const reportExpiredEl = document.getElementById('reportExpired');

    // Action buttons
    const generateLowStockReportBtn = document.getElementById('generateLowStockReportBtn');
    const generateExpiringSoonReportBtn = document.getElementById('generateExpiringSoonReportBtn');
    const deleteExpiredBtn = document.getElementById('deleteExpiredBtn');
    const actionMessageEl = document.getElementById('actionMessage');
    const logsLinkSidebar = document.getElementById('logsLinkSidebar');

    // Tab elements
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabContents = document.querySelectorAll('.tab-content');
    const lowStockTableBody = document.getElementById('lowStockTableBody');
    const expiringSoonTableBody = document.getElementById('expiringSoonTableBody');
    const expiredItemsTableBody = document.getElementById('expiredItemsTableBody');

    async function fetchReportStats() {
        try {
            const response = await fetch(`${API_BASE_URL}/report-stats`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const stats = await response.json();
            reportLowStockEl.textContent = stats.lowStockItemCount;
            reportExpiringSoonEl.textContent = stats.expiringSoonItemCount;
            reportExpiredEl.textContent = stats.expiredItemCount;
        } catch (error) {
            console.error('Error fetching report stats:', error);
            showActionMessage('Error fetching report stats.', true);
        }
    }

    function showActionMessage(message, isError = false) {
        actionMessageEl.textContent = message;
        actionMessageEl.classList.remove('hidden', 'bg-green-100', 'text-green-700', 'bg-red-100', 'text-red-700');
        if (isError) {
            actionMessageEl.classList.add('bg-red-100', 'text-red-700');
        } else {
            actionMessageEl.classList.add('bg-green-100', 'text-green-700');
        }
        actionMessageEl.classList.remove('hidden');
        setTimeout(() => actionMessageEl.classList.add('hidden'), 5000);
    }

    async function handleGenerateReportFile(url, successMessage) {
        try {
            const response = await fetch(url, { method: 'POST' });
            const result = await response.json();
            if (!response.ok) throw new Error(result.message || `HTTP error! status: ${response.status}`);
            showActionMessage(successMessage + (result.fileName ? ` File: ${result.fileName}` : ''));
            // No need to refresh generated file list here, it's on another page
        } catch (error) {
            console.error('Error generating report file:', error);
            showActionMessage(error.message || 'Error generating report file.', true);
        }
    }

    if (logsLinkSidebar) {
        logsLinkSidebar.addEventListener('click', (e) => {
            e.preventDefault();
            window.open('/api/admin/logs?limit=100', '_blank');
        });
    }

    generateLowStockReportBtn.addEventListener('click', () => {
        handleGenerateReportFile(`${API_BASE_URL}/reports/generate/low-stock`, 'Low stock report file generation initiated.');
    });

    generateExpiringSoonReportBtn.addEventListener('click', () => {
        handleGenerateReportFile(`${API_BASE_URL}/reports/generate/expiring-soon`, 'Expiring soon report file generation initiated.');
    });

    deleteExpiredBtn.addEventListener('click', async () => {
        if (!confirm('Are you sure you want to delete ALL expired items? This action cannot be undone.')) {
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/items/delete-expired`, { method: 'POST' });
            const result = await response.json();
            if (!response.ok) throw new Error(result.message || `HTTP error! status: ${response.status}`);
            showActionMessage(result.message || `Deleted ${result.deletedCount} expired items.`);
            fetchReportStats(); // Refresh stats cards
            // Refresh active live data table
            const activeTab = document.querySelector('.tab-button.border-blue-500');
            if (activeTab) {
                if (activeTab.id === 'tabLowStock') fetchLowStockData();
                else if (activeTab.id === 'tabExpiringSoon') fetchExpiringSoonData();
                else if (activeTab.id === 'tabExpiredItems') fetchExpiredItemsData();
            }
        } catch (error) {
            console.error('Error deleting expired items:', error);
            showActionMessage(error.message || 'Error deleting expired items.', true);
        }
    });

    // Tab Switching Logic
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            tabButtons.forEach(btn => {
                btn.classList.remove('border-blue-500', 'text-blue-600');
                btn.classList.add('border-transparent', 'text-gray-500', 'hover:text-gray-700', 'hover:border-gray-300');
            });
            button.classList.add('border-blue-500', 'text-blue-600');
            button.classList.remove('border-transparent', 'text-gray-500', 'hover:text-gray-700', 'hover:border-gray-300');

            const targetContentId = button.id.replace('tab', '').toLowerCase() + 'Content'; // e.g. lowstockContent
            tabContents.forEach(content => {
                if (content.id === targetContentId) {
                    content.classList.remove('hidden');
                    if (targetContentId === 'lowstockContent') fetchLowStockData();
                    else if (targetContentId === 'expiringsoonContent') fetchExpiringSoonData();
                    else if (targetContentId === 'expireditemsContent') fetchExpiredItemsData();
                } else {
                    content.classList.add('hidden');
                }
            });
        });
    });

    function renderTable(tbodyElement, items, type) {
        tbodyElement.innerHTML = '';
        if (items.length === 0) {
            const cols = type === 'expiring' || type === 'expired' ? 5 : 4;
            tbodyElement.innerHTML = `<tr><td colspan="${cols}" class="text-center py-4">No items found for this category.</td></tr>`;
            return;
        }
        items.forEach(item => {
            const row = tbodyElement.insertRow();
            let expiryDateHtml = '';
            if (type === 'expiring' || type === 'expired') {
                expiryDateHtml = `<td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${item.expiryDate}</td>`;
            }
            row.innerHTML = `
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${item.name}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${item.category}</td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${item.quantity}</td>
                ${expiryDateHtml}
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                    <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(item.status)}">
                        ${item.status}
                    </span>
                </td>
            `;
        });
    }

    function getStatusColor(status) {
        status = status.toLowerCase();
        if (status === 'low stock') return 'status-low';
        if (status === 'expired') return 'status-expired';
        if (status === 'expiring soon') return 'status-expiring-soon';
        if (status === 'in stock') return 'bg-green-100 text-green-800';
        if (status === 'out of stock') return 'bg-pink-100 text-pink-800';
        return 'bg-gray-100 text-gray-800';
    }

    async function fetchLowStockData() {
        try {
            const response = await fetch(`${API_BASE_URL}/reports/items/low-stock`);
            if (!response.ok) throw new Error('Failed to load low stock items');
            const items = await response.json();
            renderTable(lowStockTableBody, items, 'low');
        } catch (error) {
            console.error(error);
            lowStockTableBody.innerHTML = `<tr><td colspan="4" class="text-red-500 text-center py-4">${error.message}</td></tr>`;
        }
    }

    async function fetchExpiringSoonData() {
        try {
            const response = await fetch(`${API_BASE_URL}/reports/items/expiring-soon`);
            if (!response.ok) throw new Error('Failed to load expiring soon items');
            const items = await response.json();
            renderTable(expiringSoonTableBody, items, 'expiring');
        } catch (error) {
            console.error(error);
            expiringSoonTableBody.innerHTML = `<tr><td colspan="5" class="text-red-500 text-center py-4">${error.message}</td></tr>`;
        }
    }

    async function fetchExpiredItemsData() {
        try {
            const response = await fetch(`${API_BASE_URL}/reports/items/expired`);
            if (!response.ok) throw new Error('Failed to load expired items');
            const items = await response.json();
            renderTable(expiredItemsTableBody, items, 'expired');
        } catch (error) {
            console.error(error);
            expiredItemsTableBody.innerHTML = `<tr><td colspan="5" class="text-red-500 text-center py-4">${error.message}</td></tr>`;
        }
    }

    // Initial data load for the page and the first tab
    fetchReportStats();
    fetchLowStockData(); // Load data for the default visible tab
});