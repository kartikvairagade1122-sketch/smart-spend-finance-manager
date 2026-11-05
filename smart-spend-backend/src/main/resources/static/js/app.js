// Smart Spend - Core Single Page Web Application Engine
let trendsChartInstance = null;
let categoryChartInstance = null;
let portfolioChartInstance = null;
let currentTab = 'dashboard';
let cachedCategories = [];
let txSearchDebounceTimer = null;

// Initialization
document.addEventListener('DOMContentLoaded', () => {
  // Render Lucide icons
  if (window.lucide) lucide.createIcons();

  // Set default dates in modals to today
  const today = new Date().toISOString().split('T')[0];
  const txDate = document.getElementById('tx-modal-date');
  if (txDate) txDate.value = today;
  const stockDate = document.getElementById('stock-date');
  if (stockDate) stockDate.value = today;

  // Check Auth State
  if (API.isAuthenticated()) {
    initAuthenticatedApp();
  } else {
    showAuthView();
  }
});

// ================= AUTHENTICATION =================
function showAuthView() {
  document.getElementById('auth-view').classList.remove('hidden');
  document.getElementById('main-view').classList.add('hidden');
}

function initAuthenticatedApp() {
  document.getElementById('auth-view').classList.add('hidden');
  document.getElementById('main-view').classList.remove('hidden');

  const user = API.getUser();
  if (user) {
    const displayName = user.fullName || user.username || 'User';
    document.getElementById('user-display-name').textContent = displayName;
    document.getElementById('user-avatar').textContent = displayName.charAt(0).toUpperCase();
  }

  // Pre-load global categories
  loadCategoriesDropdowns();

  // Load initial tab
  navigateTo(window.location.hash.replace('#', '') || 'dashboard');
}

function switchAuthTab(tab) {
  const loginForm = document.getElementById('login-form');
  const regForm = document.getElementById('register-form');
  const loginBtn = document.getElementById('tab-login-btn');
  const regBtn = document.getElementById('tab-register-btn');

  if (tab === 'login') {
    loginForm.classList.remove('hidden');
    regForm.classList.add('hidden');
    loginBtn.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all text-white bg-blue-600 shadow';
    regBtn.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all text-slate-400 hover:text-white';
  } else {
    loginForm.classList.add('hidden');
    regForm.classList.remove('hidden');
    regBtn.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all text-white bg-blue-600 shadow';
    loginBtn.className = 'flex-1 py-2 text-xs font-bold rounded-lg transition-all text-slate-400 hover:text-white';
  }
}

function fillDemoCredentials() {
  document.getElementById('login-username').value = 'kartikdemo';
  document.getElementById('login-password').value = 'Demo@12345';
  showToast('Logging into live demo portfolio...', 'info');
  const loginForm = document.getElementById('login-form');
  if (loginForm) {
    loginForm.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
  }
}

async function handleLoginSubmit(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;
  const btn = document.getElementById('login-submit-btn');

  try {
    btn.disabled = true;
    btn.innerHTML = '<span>Signing In...</span>';
    const res = await API.login(username, password);

    if (res.data && res.data.accessToken) {
      API.setToken(res.data.accessToken);
      API.setUser(res.data.user || { username });
      showToast('Welcome back, ' + (res.data.user ? res.data.user.fullName : username) + '!', 'success');
      initAuthenticatedApp();
    } else {
      showToast('Login response invalid', 'error');
    }
  } catch (err) {
    showToast(err.message || 'Login failed. Please check your credentials.', 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span>Sign In to Dashboard</span><i data-lucide="arrow-right" class="w-4 h-4"></i>';
    if (window.lucide) lucide.createIcons();
  }
}

async function handleRegisterSubmit(e) {
  e.preventDefault();
  const fullName = document.getElementById('reg-fullname').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const username = document.getElementById('reg-username').value.trim();
  const password = document.getElementById('reg-password').value;
  const confirmPassword = document.getElementById('reg-confirm').value;

  if (password !== confirmPassword) {
    showToast('Passwords do not match!', 'error');
    return;
  }

  const btn = document.getElementById('reg-submit-btn');
  try {
    btn.disabled = true;
    btn.innerHTML = '<span>Creating Account...</span>';
    const res = await API.register(fullName, email, username, password, confirmPassword);

    if (res.data && res.data.accessToken) {
      API.setToken(res.data.accessToken);
      API.setUser(res.data.user || { username, fullName, email });
      showToast('Account registered successfully!', 'success');
      initAuthenticatedApp();
    } else {
      showToast('Registration successful! Please sign in.', 'success');
      switchAuthTab('login');
      document.getElementById('login-username').value = username;
    }
  } catch (err) {
    showToast(err.message || 'Registration failed.', 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = '<span>Create Free Account</span><i data-lucide="check-circle-2" class="w-4 h-4"></i>';
    if (window.lucide) lucide.createIcons();
  }
}

// ================= NAVIGATION =================
function navigateTo(tab) {
  const tabs = ['dashboard', 'transactions', 'income', 'expense', 'budgets', 'portfolio', 'reports', 'profile'];
  if (!tabs.includes(tab)) tab = 'dashboard';

  currentTab = tab;
  window.location.hash = `#${tab}`;

  // Hide all sections, show target
  tabs.forEach(t => {
    const sec = document.getElementById(`page-${t}`);
    const navBtn = document.getElementById(`nav-${t}`);
    if (sec) sec.classList.add('hidden');
    if (navBtn) navBtn.classList.remove('active');
  });

  const activeSec = document.getElementById(`page-${tab}`);
  const activeNav = document.getElementById(`nav-${tab}`);
  if (activeSec) activeSec.classList.remove('hidden');
  if (activeNav) activeNav.classList.add('active');

  // Trigger data loader for current tab
  switch (tab) {
    case 'dashboard':
      loadDashboard();
      break;
    case 'transactions':
      loadTransactions();
      break;
    case 'income':
      loadTypeTransactions('INCOME', 'income-table-body');
      break;
    case 'expense':
      loadTypeTransactions('EXPENSE', 'expense-table-body');
      break;
    case 'budgets':
      loadBudgets();
      break;
    case 'portfolio':
      loadPortfolio();
      break;
    case 'reports':
      loadReports();
      break;
    case 'profile':
      loadProfile();
      break;
  }

  if (window.lucide) lucide.createIcons();
}

function toggleMobileSidebar() {
  const sidebar = document.getElementById('sidebar');
  sidebar.classList.toggle('hidden');
}

// ================= DASHBOARD CONTROLLER =================
async function loadDashboard() {
  try {
    const res = await API.getDashboardSummary();
    const data = res.data;
    if (!data) return;

    // Balance Banner
    document.getElementById('dash-balance').textContent = formatINR(data.currentBalance);
    document.getElementById('dash-savings-rate').textContent = `${(data.savingsRate || 0).toFixed(1)}%`;

    // 4 KPI Cards
    document.getElementById('kpi-balance').textContent = formatINR(data.currentBalance);
    document.getElementById('kpi-income').textContent = `+${formatINR(data.monthlyIncome)}`;
    document.getElementById('kpi-expense').textContent = `-${formatINR(data.monthlyExpenses)}`;
    document.getElementById('kpi-savings').textContent = formatINR(data.monthlySavings);

    // Render Trends Chart (Income vs Expense)
    renderTrendsChart(data.monthlyTrends || []);

    // Render Expense Category Donut Chart
    renderCategoryChart(data.expenseCategoryBreakdown || []);

    // Render Recent Transactions
    renderRecentTransactions(data.recentTransactions || []);
  } catch (err) {
    console.error('Error loading dashboard summary:', err);
  }
}

function renderTrendsChart(trends) {
  const ctx = document.getElementById('chart-trends');
  if (!ctx) return;

  if (trendsChartInstance) trendsChartInstance.destroy();

  const labels = trends.map(t => t.monthYear || t.month);
  const incomeData = trends.map(t => t.income || 0);
  const expenseData = trends.map(t => t.expense || 0);

  trendsChartInstance = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: labels.length ? labels : ['No Data'],
      datasets: [
        {
          label: 'Income (₹)',
          data: incomeData.length ? incomeData : [0],
          backgroundColor: 'rgba(16, 185, 129, 0.85)',
          borderRadius: 6
        },
        {
          label: 'Expense (₹)',
          data: expenseData.length ? expenseData : [0],
          backgroundColor: 'rgba(239, 68, 68, 0.85)',
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          labels: { color: '#cbd5e1', font: { family: 'Plus Jakarta Sans', size: 12 } }
        }
      },
      scales: {
        x: {
          ticks: { color: '#94a3b8' },
          grid: { color: 'rgba(51, 65, 85, 0.3)' }
        },
        y: {
          ticks: {
            color: '#94a3b8',
            callback: val => '₹' + Number(val).toLocaleString('en-IN')
          },
          grid: { color: 'rgba(51, 65, 85, 0.3)' }
        }
      }
    }
  });
}

function renderCategoryChart(breakdown) {
  const ctx = document.getElementById('chart-categories');
  const legendBox = document.getElementById('category-legend');
  if (!ctx) return;

  if (categoryChartInstance) categoryChartInstance.destroy();

  if (!breakdown || breakdown.length === 0) {
    if (legendBox) legendBox.innerHTML = '<span class="text-slate-500">No expense data recorded.</span>';
    return;
  }

  const labels = breakdown.map(b => b.categoryName);
  const values = breakdown.map(b => b.totalAmount);
  const colors = [
    '#38bdf8', '#34d399', '#f87171', '#fbbf24', '#a78bfa',
    '#f472b6', '#22d3ee', '#fb923c', '#a3e635', '#e879f9'
  ];

  categoryChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: values,
        backgroundColor: colors.slice(0, labels.length),
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      cutout: '72%'
    }
  });

  if (legendBox) {
    legendBox.innerHTML = breakdown.map((b, idx) => `
      <div class="flex items-center justify-between py-0.5">
        <span class="flex items-center space-x-2">
          <span class="w-2.5 h-2.5 rounded-full" style="background-color: ${colors[idx % colors.length]}"></span>
          <span class="text-slate-300">${b.categoryName}</span>
        </span>
        <span class="font-bold text-slate-200">${formatINR(b.totalAmount)}</span>
      </div>
    `).join('');
  }
}

function renderRecentTransactions(transactions) {
  const tbody = document.getElementById('dash-recent-table');
  if (!tbody) return;

  if (!transactions || transactions.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" class="py-6 text-center text-xs text-slate-500">No recent transactions recorded.</td></tr>`;
    return;
  }

  tbody.innerHTML = transactions.slice(0, 5).map(tx => {
    const isIncome = tx.type === 'INCOME';
    const sign = isIncome ? '+' : '-';
    const colorClass = isIncome ? 'text-emerald-400' : 'text-red-400';
    const badgeClass = isIncome ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' : 'bg-red-500/15 text-red-400 border-red-500/30';

    return `
      <tr class="hover:bg-slate-800/40 transition">
        <td class="py-3 px-3 text-xs text-slate-400">${tx.transactionDate}</td>
        <td class="py-3 px-3 text-xs font-semibold text-white">${tx.description || '—'}</td>
        <td class="py-3 px-3 text-xs text-slate-300">
          <span class="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700">${tx.categoryName || 'General'}</span>
        </td>
        <td class="py-3 px-3">
          <span class="px-2 py-0.5 rounded-md border text-[11px] font-bold ${badgeClass}">${tx.type}</span>
        </td>
        <td class="py-3 px-3 text-right text-xs font-bold ${colorClass}">
          ${sign}${formatINR(tx.amount)}
        </td>
      </tr>
    `;
  }).join('');
}

// ================= TRANSACTIONS CONTROLLER =================
async function loadTransactions() {
  const query = document.getElementById('tx-search-query').value.trim();
  const type = document.getElementById('tx-filter-type').value;
  const categoryId = document.getElementById('tx-filter-category').value;

  try {
    const res = await API.getTransactions({ query, type, categoryId, size: 100 });
    const page = res.data;
    const content = page.content || [];

    document.getElementById('tx-count-badge').textContent = `Showing ${content.length} transactions`;

    const tbody = document.getElementById('tx-full-table');
    if (content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="6" class="py-12 text-center text-slate-500 text-xs">No transactions found matching your criteria.</td></tr>`;
      return;
    }

    tbody.innerHTML = content.map(tx => {
      const isIncome = tx.type === 'INCOME';
      const sign = isIncome ? '+' : '-';
      const colorClass = isIncome ? 'text-emerald-400 font-bold' : 'text-red-400 font-bold';
      const badgeClass = isIncome ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30' : 'bg-red-500/15 text-red-400 border-red-500/30';

      return `
        <tr class="hover:bg-slate-800/40 transition">
          <td class="py-3 px-4 text-xs text-slate-400">${tx.transactionDate}</td>
          <td class="py-3 px-4 text-xs font-semibold text-white">${tx.description || '—'}</td>
          <td class="py-3 px-4 text-xs text-slate-300">
            <span class="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700">${tx.categoryName || 'General'}</span>
          </td>
          <td class="py-3 px-4">
            <span class="px-2 py-0.5 rounded-md border text-[11px] font-bold ${badgeClass}">${tx.type}</span>
          </td>
          <td class="py-3 px-4 text-right text-sm ${colorClass}">
            ${sign}${formatINR(tx.amount)}
          </td>
          <td class="py-3 px-4 text-center">
            <button onclick="deleteTransactionItem(${tx.id})" title="Delete Transaction"
                    class="p-1 rounded text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition">
              🗑️
            </button>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    showToast('Failed to load transactions: ' + err.message, 'error');
  }
}

function debounceTxSearch() {
  clearTimeout(txSearchDebounceTimer);
  txSearchDebounceTimer = setTimeout(loadTransactions, 300);
}

async function loadTypeTransactions(type, tbodyId) {
  try {
    const res = await API.getTransactions({ type, size: 100 });
    const content = res.data.content || [];
    const tbody = document.getElementById(tbodyId);

    if (content.length === 0) {
      tbody.innerHTML = `<tr><td colspan="5" class="py-12 text-center text-slate-500 text-xs">No ${type.toLowerCase()} transactions logged yet.</td></tr>`;
      return;
    }

    tbody.innerHTML = content.map(tx => `
      <tr class="hover:bg-slate-800/40 transition">
        <td class="py-3 px-4 text-xs text-slate-400">${tx.transactionDate}</td>
        <td class="py-3 px-4 text-xs font-semibold text-white">${tx.description || '—'}</td>
        <td class="py-3 px-4 text-xs text-slate-300">
          <span class="px-2 py-0.5 rounded-md bg-slate-800 border border-slate-700">${tx.categoryName || 'General'}</span>
        </td>
        <td class="py-3 px-4 text-right text-sm ${type === 'INCOME' ? 'text-emerald-400 font-bold' : 'text-red-400 font-bold'}">
          ${type === 'INCOME' ? '+' : '-'}${formatINR(tx.amount)}
        </td>
        <td class="py-3 px-4 text-center">
          <button onclick="deleteTransactionItem(${tx.id})" class="p-1 rounded text-slate-500 hover:text-red-400 transition">
            🗑️
          </button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    showToast('Error loading ledger: ' + err.message, 'error');
  }
}

async function deleteTransactionItem(id) {
  if (!confirm('Are you sure you want to delete this transaction?')) return;
  try {
    await API.deleteTransaction(id);
    showToast('Transaction removed successfully.', 'success');
    if (currentTab === 'dashboard') loadDashboard();
    else if (currentTab === 'transactions') loadTransactions();
    else if (currentTab === 'income') loadTypeTransactions('INCOME', 'income-table-body');
    else if (currentTab === 'expense') loadTypeTransactions('EXPENSE', 'expense-table-body');
  } catch (err) {
    showToast('Could not delete transaction: ' + err.message, 'error');
  }
}

// Modal handling: Transaction
async function loadCategoriesDropdowns() {
  try {
    const res = await API.getCategories();
    cachedCategories = res.data || [];

    // Filter dropdown in transactions
    const filterCat = document.getElementById('tx-filter-category');
    if (filterCat) {
      filterCat.innerHTML = '<option value="">All Categories</option>' +
        cachedCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
    }

    // Modal category dropdown
    populateModalCategories('EXPENSE');
  } catch (e) {
    console.error('Categories load error:', e);
  }
}

function populateModalCategories(type) {
  const catSelect = document.getElementById('tx-modal-category');
  if (!catSelect) return;
  const filtered = cachedCategories.filter(c => !c.type || c.type === type);
  catSelect.innerHTML = filtered.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

function openTransactionModal(defaultType = 'EXPENSE') {
  document.getElementById('tx-edit-id').value = '';
  document.getElementById('tx-modal-title').textContent = 'Add Transaction';
  document.getElementById('tx-modal-amount').value = '';
  document.getElementById('tx-modal-desc').value = '';
  setTxModalType(defaultType);
  document.getElementById('modal-transaction').classList.remove('hidden');
}

function closeTransactionModal() {
  document.getElementById('modal-transaction').classList.add('hidden');
}

function setTxModalType(type) {
  document.getElementById('tx-modal-type').value = type;
  const expBtn = document.getElementById('tx-type-expense-btn');
  const incBtn = document.getElementById('tx-type-income-btn');

  if (type === 'EXPENSE') {
    expBtn.className = 'py-2 text-xs font-bold rounded-lg border border-red-500/40 bg-red-600/20 text-red-400';
    incBtn.className = 'py-2 text-xs font-bold rounded-lg border border-slate-700 bg-slate-800 text-slate-400';
  } else {
    incBtn.className = 'py-2 text-xs font-bold rounded-lg border border-emerald-500/40 bg-emerald-600/20 text-emerald-400';
    expBtn.className = 'py-2 text-xs font-bold rounded-lg border border-slate-700 bg-slate-800 text-slate-400';
  }
  populateModalCategories(type);
}

async function handleTransactionSave(e) {
  e.preventDefault();
  const id = document.getElementById('tx-edit-id').value;
  const type = document.getElementById('tx-modal-type').value;
  const amount = parseFloat(document.getElementById('tx-modal-amount').value);
  const categoryId = parseInt(document.getElementById('tx-modal-category').value);
  const description = document.getElementById('tx-modal-desc').value.trim();
  const transactionDate = document.getElementById('tx-modal-date').value;

  if (isNaN(amount) || amount <= 0) {
    showToast('Please enter a valid amount greater than ₹0.00', 'error');
    return;
  }

  const payload = { type, amount, categoryId, description, transactionDate };

  try {
    if (id) {
      await API.updateTransaction(id, payload);
      showToast('Transaction updated successfully.', 'success');
    } else {
      await API.createTransaction(payload);
      showToast('Transaction added successfully!', 'success');
    }
    closeTransactionModal();
    if (currentTab === 'dashboard') loadDashboard();
    else if (currentTab === 'transactions') loadTransactions();
    else if (currentTab === 'income') loadTypeTransactions('INCOME', 'income-table-body');
    else if (currentTab === 'expense') loadTypeTransactions('EXPENSE', 'expense-table-body');
  } catch (err) {
    showToast('Failed to save transaction: ' + err.message, 'error');
  }
}

// ================= LIVE STOCKS & PORTFOLIO CONTROLLER =================
async function loadPortfolio() {
  try {
    const res = await API.getPortfolio();
    const data = res.data;
    if (!data) return;

    // 4 KPI Cards
    document.getElementById('port-invested').textContent = formatINR(data.totalInvested);
    document.getElementById('port-current-val').textContent = formatINR(data.totalCurrentValue);

    const pnl = data.totalPnl || 0;
    const pnlPct = data.totalPnlPercent || 0;
    const isProfit = pnl >= 0;
    const pnlEl = document.getElementById('port-pnl');
    pnlEl.textContent = `${isProfit ? '+' : ''}${formatINR(pnl)} (${isProfit ? '+' : ''}${pnlPct.toFixed(2)}%)`;
    pnlEl.className = `text-2xl font-extrabold ${isProfit ? 'text-emerald-400' : 'text-red-400'}`;

    document.getElementById('port-pnl-subtext').textContent =
      `${data.profitableHoldingsCount || 0} in profit / ${data.lossHoldingsCount || 0} in loss`;
    document.getElementById('port-holdings-count').textContent = `${data.totalHoldingsCount || 0} Stocks`;
    document.getElementById('port-last-updated').textContent = `Updated: ${new Date().toLocaleTimeString('en-IN')}`;

    // Holdings Table
    renderPortfolioTable(data.holdings || []);

    // Portfolio Allocation Donut Chart
    renderPortfolioPieChart(data.holdings || []);
  } catch (err) {
    console.error('Portfolio fetch error:', err);
    showToast('Failed to load portfolio rates: ' + err.message, 'error');
  }
}

function renderPortfolioTable(holdings) {
  const tbody = document.getElementById('portfolio-table-body');
  if (!tbody) return;

  if (!holdings || holdings.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" class="py-10 text-center text-slate-500 text-xs">
          No stock investments recorded yet. Click "+ Add Stock" to begin tracking your portfolio!
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = holdings.map(h => {
    const isProfit = (h.pnlAmount || 0) >= 0;
    const pnlColor = isProfit ? 'text-emerald-400 font-bold' : 'text-red-400 font-bold';

    return `
      <tr class="hover:bg-slate-800/40 transition">
        <td class="py-3 px-3 text-xs font-black text-white">${h.symbol}</td>
        <td class="py-3 px-3 text-xs text-slate-300 max-w-[140px] truncate" title="${h.companyName}">${h.companyName}</td>
        <td class="py-3 px-2 text-center text-xs font-semibold text-white">${h.quantity}</td>
        <td class="py-3 px-3 text-right text-xs text-slate-300">${formatINR(h.buyPrice)}</td>
        <td class="py-3 px-3 text-right text-xs font-semibold text-emerald-400">${formatINR(h.livePrice)}</td>
        <td class="py-3 px-3 text-right text-xs font-extrabold text-sky-400">${formatINR(h.currentValue)}</td>
        <td class="py-3 px-3 text-right text-xs ${pnlColor}">
          ${isProfit ? '+' : ''}${formatINR(h.pnlAmount)} (${(h.pnlPercent || 0).toFixed(2)}%)
        </td>
        <td class="py-3 px-2 text-center">
          <button onclick="deleteStockItem(${h.id}, '${h.symbol}')" title="Remove Holding"
                  class="p-1 rounded text-slate-500 hover:text-red-400 hover:bg-red-500/10 transition">
            🗑️
          </button>
        </td>
      </tr>
    `;
  }).join('');
}

function renderPortfolioPieChart(holdings) {
  const ctx = document.getElementById('chart-portfolio-pie');
  const legendBox = document.getElementById('portfolio-legend');
  if (!ctx) return;

  if (portfolioChartInstance) portfolioChartInstance.destroy();

  const validHoldings = (holdings || []).filter(h => (h.currentValue || 0) > 0);
  if (validHoldings.length === 0) {
    if (legendBox) legendBox.innerHTML = '<span class="text-slate-500">No active assets.</span>';
    return;
  }

  const labels = validHoldings.map(h => h.symbol);
  const values = validHoldings.map(h => h.currentValue);
  const colors = ['#f59e0b', '#3b82f6', '#10b981', '#ec4899', '#8b5cf6', '#06b6d4'];

  portfolioChartInstance = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: values,
        backgroundColor: colors.slice(0, labels.length),
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      cutout: '70%'
    }
  });

  if (legendBox) {
    legendBox.innerHTML = validHoldings.map((h, idx) => `
      <div class="flex items-center justify-between py-0.5">
        <span class="flex items-center space-x-2">
          <span class="w-2.5 h-2.5 rounded-full" style="background-color: ${colors[idx % colors.length]}"></span>
          <span class="text-slate-300">${h.symbol}</span>
        </span>
        <span class="font-bold text-slate-200">${formatINR(h.currentValue)}</span>
      </div>
    `).join('');
  }
}

async function deleteStockItem(id, symbol) {
  if (!confirm(`Are you sure you want to remove ${symbol} from your portfolio?`)) return;
  try {
    await API.deleteStockHolding(id);
    showToast(`Removed ${symbol} from portfolio.`, 'success');
    loadPortfolio();
  } catch (err) {
    showToast('Failed to delete holding: ' + err.message, 'error');
  }
}

// Modal: Stock
function openStockModal() {
  document.getElementById('stock-edit-id').value = '';
  document.getElementById('stock-symbol').value = '';
  document.getElementById('stock-company').value = '';
  document.getElementById('stock-qty').value = '';
  document.getElementById('stock-price').value = '';
  document.getElementById('stock-calc-preview').textContent = '₹0.00';
  document.getElementById('stock-quote-banner').classList.add('hidden');
  document.getElementById('stock-modal-error').classList.add('hidden');
  document.getElementById('modal-stock').classList.remove('hidden');
}

function closeStockModal() {
  document.getElementById('modal-stock').classList.add('hidden');
}

function updateStockInvestedPreview() {
  const qty = parseInt(document.getElementById('stock-qty').value) || 0;
  const price = parseFloat(document.getElementById('stock-price').value) || 0;
  const preview = document.getElementById('stock-calc-preview');
  if (qty > 0 && price > 0) {
    preview.textContent = formatINR(qty * price);
  } else {
    preview.textContent = '₹0.00';
  }
}

async function fetchLiveQuotePreview() {
  const symbol = document.getElementById('stock-symbol').value.trim();
  const errorBox = document.getElementById('stock-modal-error');
  const banner = document.getElementById('stock-quote-banner');
  const btn = document.getElementById('stock-fetch-btn');

  if (!symbol) {
    errorBox.textContent = 'Please enter a stock symbol first.';
    errorBox.classList.remove('hidden');
    return;
  }

  errorBox.classList.add('hidden');
  btn.disabled = true;
  btn.textContent = 'Fetching...';

  try {
    const res = await API.getLiveQuote(symbol);
    const quote = res.data;

    if (!quote || !quote.valid || quote.currentPrice <= 0) {
      banner.classList.add('hidden');
      errorBox.textContent = `⚠️ Stock symbol '${symbol}' not found on NSE/BSE. Please check ticker (e.g. RELIANCE, TCS, INFY, TATAMOTORS, HDFCBANK).`;
      errorBox.classList.remove('hidden');
      return;
    }

    // Populate live banner
    document.getElementById('stock-banner-name').textContent = quote.companyName || symbol;
    document.getElementById('stock-banner-price').textContent = formatINR(quote.currentPrice);
    const change = quote.dayChangePercent || 0;
    const sign = change >= 0 ? '+' : '';
    document.getElementById('stock-banner-change').textContent = `${sign}${change.toFixed(2)}%`;

    // Pre-fill inputs
    if (!document.getElementById('stock-company').value) {
      document.getElementById('stock-company').value = quote.companyName || symbol;
    }
    if (!document.getElementById('stock-price').value) {
      document.getElementById('stock-price').value = quote.currentPrice;
      updateStockInvestedPreview();
    }

    banner.classList.remove('hidden');
  } catch (err) {
    banner.classList.add('hidden');
    errorBox.textContent = 'Could not fetch live rate: ' + err.message;
    errorBox.classList.remove('hidden');
  } finally {
    btn.disabled = false;
    btn.textContent = '🔍 Live Price';
  }
}

async function handleStockSave(e) {
  e.preventDefault();
  const symbol = document.getElementById('stock-symbol').value.trim();
  const companyName = document.getElementById('stock-company').value.trim() || symbol;
  const quantity = parseInt(document.getElementById('stock-qty').value);
  const buyPrice = parseFloat(document.getElementById('stock-price').value);
  const buyDate = document.getElementById('stock-date').value;

  if (!symbol || isNaN(quantity) || quantity <= 0 || isNaN(buyPrice) || buyPrice <= 0) {
    showToast('Please provide a valid symbol, quantity (>0) and purchase price (>₹0.00)', 'error');
    return;
  }

  const payload = { symbol, companyName, quantity, buyPrice, buyDate };
  const saveBtn = document.getElementById('stock-save-btn');

  try {
    saveBtn.disabled = true;
    saveBtn.textContent = 'Saving...';
    await API.addStockHolding(payload);
    showToast(`Added ${symbol} to your portfolio!`, 'success');
    closeStockModal();
    loadPortfolio();
  } catch (err) {
    showToast('Failed to save stock: ' + err.message, 'error');
  } finally {
    saveBtn.disabled = false;
    saveBtn.textContent = '💾 Save to Portfolio';
  }
}

// ================= BUDGETS CONTROLLER =================
async function loadBudgets() {
  try {
    const res = await API.getBudgets();
    const budgets = res.data || [];
    const grid = document.getElementById('budgets-grid');

    if (budgets.length === 0) {
      grid.innerHTML = `
        <div class="col-span-full py-16 text-center text-slate-500 text-xs glass-panel rounded-2xl border border-slate-800">
          No active budgets set for this month. Click "+ Set Budget" to start budgeting!
        </div>
      `;
      return;
    }

    grid.innerHTML = budgets.map(b => {
      const pct = Math.min(b.percentageSpent || 0, 100);
      const isOver = (b.percentageSpent || 0) >= 100;
      const isWarn = (b.percentageSpent || 0) >= 80 && !isOver;
      const barColor = isOver ? 'bg-red-500' : isWarn ? 'bg-amber-500' : 'bg-emerald-500';

      return `
        <div class="glass-panel card-hover rounded-2xl p-5 border border-slate-800 space-y-3">
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-white">${b.categoryName}</span>
            <span class="text-xs font-bold ${isOver ? 'text-red-400' : 'text-slate-400'}">
              ${(b.percentageSpent || 0).toFixed(0)}%
            </span>
          </div>

          <div class="w-full bg-slate-800 rounded-full h-2.5 overflow-hidden">
            <div class="${barColor} h-2.5 rounded-full transition-all duration-500" style="width: ${pct}%"></div>
          </div>

          <div class="flex items-center justify-between text-xs text-slate-400 pt-1">
            <span>Spent: <b class="text-white">${formatINR(b.currentSpent)}</b></span>
            <span>Limit: <b class="text-white">${formatINR(b.monthlyLimit)}</b></span>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    showToast('Failed to load budgets: ' + err.message, 'error');
  }
}

function openBudgetModal() {
  const catSelect = document.getElementById('budget-category');
  const expenseCategories = cachedCategories.filter(c => c.type === 'EXPENSE');
  catSelect.innerHTML = expenseCategories.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
  document.getElementById('budget-amount').value = '';
  document.getElementById('modal-budget').classList.remove('hidden');
}

function closeBudgetModal() {
  document.getElementById('modal-budget').classList.add('hidden');
}

async function handleBudgetSave(e) {
  e.preventDefault();
  const categoryId = parseInt(document.getElementById('budget-category').value);
  const monthlyLimit = parseFloat(document.getElementById('budget-amount').value);

  if (isNaN(monthlyLimit) || monthlyLimit <= 0) {
    showToast('Please enter a valid monthly limit greater than ₹0.00', 'error');
    return;
  }

  const now = new Date();
  const payload = {
    categoryId,
    monthlyLimit,
    month: now.getMonth() + 1,
    year: now.getFullYear()
  };

  try {
    await API.createBudget(payload);
    showToast('Budget target created successfully!', 'success');
    closeBudgetModal();
    loadBudgets();
  } catch (err) {
    showToast('Could not save budget: ' + err.message, 'error');
  }
}

// ================= REPORTS CONTROLLER =================
async function loadReports() {
  const period = document.getElementById('report-period').value;
  try {
    const res = await API.getReportSummary(period);
    const data = res.data;
    const content = document.getElementById('report-summary-content');

    if (!data) return;

    content.innerHTML = `
      <div class="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div class="p-4 rounded-xl bg-slate-800/60 border border-slate-700">
          <span class="text-xs text-slate-400 block">Total Inflow</span>
          <span class="text-lg font-bold text-emerald-400">+${formatINR(data.totalIncome)}</span>
        </div>
        <div class="p-4 rounded-xl bg-slate-800/60 border border-slate-700">
          <span class="text-xs text-slate-400 block">Total Outflow</span>
          <span class="text-lg font-bold text-red-400">-${formatINR(data.totalExpenses)}</span>
        </div>
        <div class="p-4 rounded-xl bg-slate-800/60 border border-slate-700">
          <span class="text-xs text-slate-400 block">Net Surplus</span>
          <span class="text-lg font-bold text-sky-400">${formatINR(data.netSavings)}</span>
        </div>
      </div>
      <p class="text-xs text-slate-400 pt-3">
        Total of <b>${data.transactionCount || 0}</b> transactions analyzed for period: <b>${period}</b>.
      </p>
    `;
  } catch (err) {
    console.error('Report load error:', err);
  }
}

async function handleExportCsv() {
  try {
    const blob = await API.exportCsv('ALL');
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `smartspend-transactions-${new Date().toISOString().split('T')[0]}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    showToast('CSV report downloaded successfully!', 'success');
  } catch (err) {
    showToast('Export failed: ' + err.message, 'error');
  }
}

// ================= PROFILE CONTROLLER =================
async function loadProfile() {
  try {
    const res = await API.getProfile();
    const data = res.data;
    if (data) {
      document.getElementById('prof-fullname').value = data.fullName || '';
      document.getElementById('prof-email').value = data.email || '';
    }
  } catch (err) {
    console.error('Profile fetch error:', err);
  }
}

async function handleProfileUpdate(e) {
  e.preventDefault();
  const fullName = document.getElementById('prof-fullname').value.trim();
  const email = document.getElementById('prof-email').value.trim();

  try {
    await API.updateProfile({ fullName, email });
    const user = API.getUser() || {};
    user.fullName = fullName;
    user.email = email;
    API.setUser(user);
    document.getElementById('user-display-name').textContent = fullName;
    showToast('Profile updated successfully!', 'success');
  } catch (err) {
    showToast('Failed to update profile: ' + err.message, 'error');
  }
}

async function handleChangePassword(e) {
  e.preventDefault();
  const currentPassword = document.getElementById('pwd-current').value;
  const newPassword = document.getElementById('pwd-new').value;
  const confirmNewPassword = document.getElementById('pwd-confirm').value;

  if (newPassword !== confirmNewPassword) {
    showToast('New passwords do not match!', 'error');
    return;
  }

  try {
    await API.changePassword(currentPassword, newPassword, confirmNewPassword);
    showToast('Password changed successfully!', 'success');
    document.getElementById('pwd-current').value = '';
    document.getElementById('pwd-new').value = '';
    document.getElementById('pwd-confirm').value = '';
  } catch (err) {
    showToast('Failed to change password: ' + err.message, 'error');
  }
}
