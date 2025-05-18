document.addEventListener('DOMContentLoaded', function () {
    const API_BASE_URL = '/api/admin';

    const totalProductsEl = document.getElementById('totalProducts');
    const lowStockItemsEl = document.getElementById('lowStockItems');
    const inventoryValueEl = document.getElementById('inventoryValue');
    const currentStockTableBody = document.getElementById('currentStockTableBody');
    const recentActivityList = document.getElementById('recentActivityList');
    const logsLink = document.getElementById('logsLink');


    async function fetchDashboardStats() {
        try {
            const response = await fetch(`${API_BASE_URL}/dashboard-stats`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const stats = await response.json();

            totalProductsEl.textContent = stats.totalProducts;
            lowStockItemsEl.textContent = stats.lowStockItemCount;
            inventoryValueEl.textContent = `$${stats.totalInventoryValue.toFixed(2)}`;
        } catch (error) {
            console.error('Error fetching dashboard stats:', error);
            totalProductsEl.textContent = 'Error';
            lowStockItemsEl.textContent = 'Error';
            inventoryValueEl.textContent = 'Error';
        }
    }

    async function fetchCurrentStock() {
        try {
            const response = await fetch(`${API_BASE_URL}/stock/current`);
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const items = await response.json();

            currentStockTableBody.innerHTML = ''; // Clear existing rows
            if (items.length === 0) {
                currentStockTableBody.innerHTML = `<tr><td colspan="4" class="text-center py-4">No stock data available.</td></tr>`;
                return;
            }
            items.forEach(item => {
                const row = currentStockTableBody.insertRow();
                row.innerHTML = `
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">${item.name}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${item.category}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${item.quantity}</td>
                    <td class="px-6 py-4 whitespace-nowrap text-sm">
                        <span class="px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(item.status)}">
                            ${item.status}
                        </span>
                    </td>
                `;
            });
        } catch (error) {
            console.error('Error fetching current stock:', error);
            currentStockTableBody.innerHTML = `<tr><td colspan="4" class="text-center py-4 text-red-500">Error loading stock data.</td></tr>`;
        }
    }

    async function fetchRecentActivity() {
        try {
            const response = await fetch(`${API_BASE_URL}/logs?limit=5`); // Get latest 5 logs
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const logs = await response.json();

            recentActivityList.innerHTML = ''; // Clear existing
            if (logs.length === 0) {
                recentActivityList.innerHTML = `<li class="text-gray-500">No recent activity.</li>`;
                return;
            }
            logs.forEach(log => {
                const listItem = document.createElement('li');
                listItem.className = 'border-b border-gray-200 pb-2';
                listItem.innerHTML = `
                    <span class="font-medium text-gray-700">${log.action}</span>
                    <span class="block text-xs text-gray-400">${new Date(log.timestamp).toLocaleString()}</span>
                `;
                recentActivityList.appendChild(listItem);
            });
        } catch (error) {
            console.error('Error fetching recent activity:', error);
            recentActivityList.innerHTML = `<li class="text-red-500">Error loading activity logs.</li>`;
        }
    }

    if (logsLink) {
        logsLink.addEventListener('click', (e) => {
            e.preventDefault();
            // For simplicity, just opening the logs page. A modal would be more advanced.
            window.open('/api/admin/logs?limit=100', '_blank');
        });
    }


    function getStatusColor(status) {
        switch (status.toLowerCase()) {
            case 'in stock': return 'bg-green-100 text-green-800';
            case 'low stock': return 'bg-orange-100 text-orange-800';
            case 'out of stock': return 'bg-red-100 text-red-800';
            case 'expired': return 'bg-red-200 text-red-900';
            default: return 'bg-gray-100 text-gray-800';
        }
    }

    // Initial data load
    fetchDashboardStats();
    fetchCurrentStock();
    fetchRecentActivity();
});