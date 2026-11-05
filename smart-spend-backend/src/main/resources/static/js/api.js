// Smart Spend - Centralized API Service & Helpers
const API = {
  BASE_URL: '/api',

  getToken() {
    return localStorage.getItem('smartspend_token');
  },

  setToken(token) {
    if (token) {
      localStorage.setItem('smartspend_token', token);
    } else {
      localStorage.removeItem('smartspend_token');
    }
  },

  getUser() {
    try {
      const u = localStorage.getItem('smartspend_user');
      return u ? JSON.parse(u) : null;
    } catch (e) {
      return null;
    }
  },

  setUser(user) {
    if (user) {
      localStorage.setItem('smartspend_user', JSON.stringify(user));
    } else {
      localStorage.removeItem('smartspend_user');
    }
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  logout() {
    this.setToken(null);
    this.setUser(null);
    window.location.hash = '#login';
    window.location.reload();
  },

  async request(endpoint, options = {}) {
    const url = `${this.BASE_URL}${endpoint}`;
    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...options.headers
    };

    const token = this.getToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, { ...options, headers });

      if (response.status === 401) {
        // Token expired or invalid
        if (this.getToken()) {
          this.setToken(null);
          this.setUser(null);
          showToast('Session expired. Please log in again.', 'warning');
          setTimeout(() => window.location.reload(), 1200);
        }
        throw new Error('Authentication required');
      }

      // Handle binary download (CSV export)
      if (options.responseType === 'blob') {
        if (!response.ok) throw new Error('File download failed');
        return await response.blob();
      }

      const json = await response.json();
      if (!response.ok) {
        throw new Error(json.message || `Request failed with status ${response.status}`);
      }
      return json;
    } catch (err) {
      console.error(`API Error [${endpoint}]:`, err);
      throw err;
    }
  },

  // Auth Endpoints
  login(username, password) {
    return this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
  },

  register(fullName, email, username, password, confirmPassword) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ fullName, email, username, password, confirmPassword })
    });
  },

  changePassword(currentPassword, newPassword, confirmNewPassword) {
    return this.request('/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ currentPassword, newPassword, confirmNewPassword })
    });
  },

  // Dashboard
  getDashboardSummary() {
    return this.request('/dashboard/summary');
  },

  // Transactions
  getTransactions(params = {}) {
    const qs = new URLSearchParams();
    if (params.type) qs.append('type', params.type);
    if (params.categoryId) qs.append('categoryId', params.categoryId);
    if (params.startDate) qs.append('startDate', params.startDate);
    if (params.endDate) qs.append('endDate', params.endDate);
    if (params.query) qs.append('query', params.query);
    if (params.page !== undefined) qs.append('page', params.page);
    if (params.size !== undefined) qs.append('size', params.size);

    const qStr = qs.toString();
    return this.request(`/transactions${qStr ? '?' + qStr : ''}`);
  },

  createTransaction(data) {
    return this.request('/transactions', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateTransaction(id, data) {
    return this.request(`/transactions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteTransaction(id) {
    return this.request(`/transactions/${id}`, {
      method: 'DELETE'
    });
  },

  // Categories
  getCategories(type) {
    const qs = type ? `?type=${type}` : '';
    return this.request(`/categories${qs}`);
  },

  createCategory(data) {
    return this.request('/categories', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  // Budgets
  getBudgets(month, year) {
    const qs = new URLSearchParams();
    if (month) qs.append('month', month);
    if (year) qs.append('year', year);
    const qStr = qs.toString();
    return this.request(`/budgets${qStr ? '?' + qStr : ''}`);
  },

  createBudget(data) {
    return this.request('/budgets', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateBudget(id, data) {
    return this.request(`/budgets/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteBudget(id) {
    return this.request(`/budgets/${id}`, {
      method: 'DELETE'
    });
  },

  // Portfolio & Live Stocks
  getPortfolio() {
    return this.request('/portfolio');
  },

  getLiveQuote(symbol) {
    return this.request(`/portfolio/quote/${encodeURIComponent(symbol)}`);
  },

  addStockHolding(data) {
    return this.request('/portfolio/holdings', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  },

  updateStockHolding(id, data) {
    return this.request(`/portfolio/holdings/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  },

  deleteStockHolding(id) {
    return this.request(`/portfolio/holdings/${id}`, {
      method: 'DELETE'
    });
  },

  // Reports
  getReportSummary(period, startDate, endDate) {
    const qs = new URLSearchParams();
    if (period) qs.append('period', period);
    if (startDate) qs.append('startDate', startDate);
    if (endDate) qs.append('endDate', endDate);
    return this.request(`/reports/summary?${qs.toString()}`);
  },

  exportCsv(period, startDate, endDate) {
    const qs = new URLSearchParams();
    if (period) qs.append('period', period);
    if (startDate) qs.append('startDate', startDate);
    if (endDate) qs.append('endDate', endDate);
    return this.request(`/reports/export/csv?${qs.toString()}`, {
      responseType: 'blob'
    });
  },

  // Profile
  getProfile() {
    return this.request('/users/me');
  },

  updateProfile(data) {
    return this.request('/users/me', {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }
};

// Indian Rupee (₹ / INR) Currency Formatter
function formatINR(amount) {
  if (amount === null || amount === undefined || isNaN(amount)) {
    return '₹0.00';
  }
  const num = typeof amount === 'string' ? parseFloat(amount) : amount;
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(num);
}

// Toast Alert System
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  const bgColors = {
    success: 'bg-emerald-600 border-emerald-500 text-white',
    error: 'bg-red-600 border-red-500 text-white',
    warning: 'bg-amber-600 border-amber-500 text-white',
    info: 'bg-blue-600 border-blue-500 text-white'
  };

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ'
  };

  toast.className = `toast flex items-center space-x-3 px-4 py-3 rounded-xl border shadow-xl text-sm font-semibold ${bgColors[type] || bgColors.info}`;
  toast.innerHTML = `
    <span class="text-base font-bold">${icons[type] || '•'}</span>
    <span>${message}</span>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}
