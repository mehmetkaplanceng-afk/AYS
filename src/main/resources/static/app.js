let currentUser = null;

// ================= Toast & Notifications =================
function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast show ${isError ? 'toast-error' : 'toast-success'}`;
    setTimeout(() => {
        toast.className = 'toast';
    }, 3500);
}

// ================= Cookie Helpers =================
function setCookie(name, value, days) {
    let expires = "";
    if (days) {
        let date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + encodeURIComponent(value || "") + expires + "; path=/; SameSite=Lax";
}

function getCookie(name) {
    let nameEQ = name + "=";
    let ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return decodeURIComponent(c.substring(nameEQ.length, c.length));
    }
    return null;
}

function eraseCookie(name) {
    document.cookie = name + '=; Max-Age=-99999999; path=/;';
}

// ================= Tab Switchers =================
function switchAuthTab(type) {
    const loginBtn = document.getElementById('tabLoginBtn');
    const registerBtn = document.getElementById('tabRegisterBtn');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (type === 'login') {
        loginBtn.classList.add('active');
        registerBtn.classList.remove('active');
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
    } else {
        registerBtn.classList.add('active');
        loginBtn.classList.remove('active');
        registerForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
    }
}

function switchAdminTab(paneId, btn) {
    document.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));

    const activePane = document.getElementById(paneId);
    if (activePane) activePane.classList.remove('hidden');
    if (btn) btn.classList.add('active');
}

function switchUserTab(paneId, btn) {
    // Only toggle within userReadonlySection context
    const section = document.getElementById('userReadonlySection');
    section.querySelectorAll('.tab-pane').forEach(p => p.classList.add('hidden'));
    section.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));

    const activePane = document.getElementById(paneId);
    if (activePane) activePane.classList.remove('hidden');
    if (btn) btn.classList.add('active');
}

// ================= Unified API Fetcher =================
async function apiCall(endpoint, options = {}) {
    options.headers = options.headers || {};
    if (currentUser && currentUser.token) {
        options.headers['Authorization'] = 'Bearer ' + currentUser.token;
    }
    if (!(options.body instanceof FormData) && !options.headers['Content-Type']) {
        options.headers['Content-Type'] = 'application/json';
    }

    const res = await fetch(endpoint, options);
    if (!res.ok) {
        let errorMsg = 'İşlem gerçekleştirilemedi.';
        try {
            const data = await res.json();
            errorMsg = data.message || errorMsg;
        } catch (_) {
            const text = await res.text();
            if (text) errorMsg = text;
        }
        throw new Error(errorMsg);
    }

    const text = await res.text();
    return text ? JSON.parse(text) : {};
}

// ================= Formatting =================
function formatMoney(num) {
    if (num == null) return '0.00 ₺';
    return Number(num).toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' ₺';
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return isNaN(d) ? dateStr : d.toLocaleDateString('tr-TR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function renderStatusBadge(status) {
    if (!status) return `<span class="badge badge-pending">Bekliyor</span>`;
    if (status.toLowerCase().includes('onay')) {
        return `<span class="badge badge-approved">Onaylandı</span>`;
    }
    if (status.toLowerCase().includes('red')) {
        return `<span class="badge badge-rejected">Reddedildi</span>`;
    }
    return `<span class="badge badge-pending">${status}</span>`;
}

// ================= App Shell Rendering =================
function renderApp() {
    const authScreen = document.getElementById('authScreen');
    const appShell = document.getElementById('appShell');

    if (!currentUser) {
        authScreen.classList.remove('hidden');
        appShell.classList.add('hidden');
        return;
    }

    authScreen.classList.add('hidden');
    appShell.classList.remove('hidden');

    // Header User Profile
    const userNameEl = document.getElementById('userName');
    const userRoleEl = document.getElementById('userRoleBadge');
    const userAvatarEl = document.getElementById('userAvatar');
    const swaggerLink = document.getElementById('swaggerLink');
    const welcomeTitle = document.getElementById('welcomeTitle');
    const welcomeSubtitle = document.getElementById('welcomeSubtitle');

    userNameEl.textContent = currentUser.fullName || currentUser.email;
    userAvatarEl.textContent = (currentUser.fullName || currentUser.email || 'A').charAt(0).toUpperCase();

    const isAdmin = currentUser.role === 'ADMIN';

    if (isAdmin) {
        userRoleEl.textContent = 'Site Yönetimi';
        swaggerLink.classList.remove('hidden');
        welcomeTitle.textContent = `Yönetici Paneli - Hoş Geldiniz, ${currentUser.fullName}`;
        welcomeSubtitle.textContent = 'Kasa hesapları, sakin bildirimleri ve aidatları bu ekrandan yönetebilirsiniz.';

        document.getElementById('adminPanel').classList.remove('hidden');
        document.getElementById('userPanel').classList.add('hidden');
        document.getElementById('userReadonlySection').classList.add('hidden');

        loadAdminData();
    } else {
        userRoleEl.textContent = 'Site Sakini';
        swaggerLink.classList.add('hidden');
        welcomeTitle.textContent = `Hoş Geldiniz, ${currentUser.fullName}`;
        welcomeSubtitle.textContent = 'Apartman aidat ve ödemelerinizi buradan yöneticiye bildirebilir ve onay durumunu takip edebilirsiniz.';

        document.getElementById('userPanel').classList.remove('hidden');
        document.getElementById('adminPanel').classList.add('hidden');
        document.getElementById('userReadonlySection').classList.remove('hidden');

        loadUserData();
    }
}

// ================= Load Admin Data =================
async function loadAdminData() {
    try {
        // 1. Ödeme Bildirimleri
        const notifications = await apiCall('/api/notifications');
        renderAdminNotifications(notifications);

        // 2. Banka Hesapları
        const accounts = await apiCall('/api/bank-accounts');
        renderBankAccounts(accounts);

        // 3. Aidatlar
        const dues = await apiCall('/api/dues');
        renderDues(dues);

        // 4. Giderler
        const expenses = await apiCall('/api/expenses');
        renderExpenses(expenses);

        // 5. Metrikleri Güncelle
        calculateMetrics(notifications, accounts, dues, expenses);
    } catch (err) {
        console.error('Veri yükleme hatası:', err);
    }
}

function calculateMetrics(notifications, accounts, dues, expenses) {
    // Toplam Bakiye
    let totalBal = 0;
    (accounts || []).forEach(a => totalBal += (Number(a.balance) || 0));
    document.getElementById('metricTotalBalance').textContent = formatMoney(totalBal);

    // Bekleyen Bildirimler
    const pendingCount = (notifications || []).filter(n => (n.status || '').toLowerCase().includes('bekli')).length;
    document.getElementById('metricPendingCount').textContent = pendingCount;

    // Toplam Aidat
    let totalDues = 0;
    (dues || []).forEach(d => totalDues += (Number(d.amount) || 0));
    document.getElementById('metricTotalDues').textContent = formatMoney(totalDues);

    // Toplam Gider
    let totalExp = 0;
    (expenses || []).forEach(e => totalExp += (Number(e.amount) || 0));
    document.getElementById('metricTotalExpenses').textContent = formatMoney(totalExp);
}

function renderAdminNotifications(list) {
    const tbody = document.getElementById('adminNotificationsTable');
    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state">Henüz bekleyen veya onaylanmış bir bildirim bulunmuyor.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(n => {
        const isPending = (n.status || '').toLowerCase().includes('bekli');
        const userLabel = n.user ? `${n.user.fullName} (${n.user.email})` : 'Bilinmeyen Kullanıcı';
        return `
            <tr>
                <td><strong>${userLabel}</strong></td>
                <td><span style="font-weight: 700; color: var(--primary);">${formatMoney(n.amount)}</span></td>
                <td>${n.description || '-'}</td>
                <td>${formatDate(n.notificationDate)}</td>
                <td>${renderStatusBadge(n.status)}</td>
                <td style="text-align: right;">
                    ${isPending ? `
                        <div class="table-actions" style="justify-content: flex-end;">
                            <button onclick="updateNotificationStatus(${n.id}, 'Onaylandı')" class="btn btn-success btn-sm">Onayla</button>
                            <button onclick="updateNotificationStatus(${n.id}, 'Reddedildi')" class="btn btn-danger btn-sm">Reddet</button>
                        </div>
                    ` : `<span style="font-size: 12px; color: var(--text-muted);">İşlem tamamlandı</span>`}
                </td>
            </tr>
        `;
    }).join('');
}

function renderBankAccounts(list) {
    const tbody = document.getElementById('bankTableBody');
    const select = document.getElementById('expenseAccountSelect');

    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="3" class="empty-state">Kayıtlı banka hesabı veya kasa bulunmuyor.</td></tr>`;
        if (select) select.innerHTML = '<option value="">Önce bir hesap ekleyiniz</option>';
        return;
    }

    tbody.innerHTML = list.map(acc => `
        <tr>
            <td><strong>${acc.accountName}</strong></td>
            <td><code style="font-size: 13px; color: var(--text-secondary);">${acc.iban}</code></td>
            <td><strong style="color: var(--success);">${formatMoney(acc.balance)}</strong></td>
        </tr>
    `).join('');

    if (select) {
        select.innerHTML = list.map(acc => `
            <option value="${acc.id}">${acc.accountName} (${formatMoney(acc.balance)})</option>
        `).join('');
    }
}

function renderDues(list) {
    const tbody = document.getElementById('duesTableBody');
    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Kayıtlı aidat çizelgesi bulunmuyor.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(d => `
        <tr>
            <td><strong>${d.apartmentNo}</strong></td>
            <td><strong>${formatMoney(d.amount)}</strong></td>
            <td>${formatDate(d.dueDate)}</td>
            <td>${d.status === 'PAID' ? '<span class="badge badge-approved">Ödendi</span>' : '<span class="badge badge-pending">Bekliyor</span>'}</td>
        </tr>
    `).join('');
}

function renderExpenses(list) {
    const tbody = document.getElementById('expensesTableBody');
    if (!list || list.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Kayıtlı gider bulunmuyor.</td></tr>`;
        return;
    }

    tbody.innerHTML = list.map(e => `
        <tr>
            <td><strong>${e.category}</strong></td>
            <td>${e.description}</td>
            <td><strong style="color: var(--danger);">${formatMoney(e.amount)}</strong></td>
            <td>${formatDate(e.expenseDate)}</td>
        </tr>
    `).join('');
}

// ================= Notification Status Action =================
async function updateNotificationStatus(id, newStatus) {
    try {
        await apiCall(`/api/notifications/${id}/status?status=${encodeURIComponent(newStatus)}`, { method: 'POST' });
        showToast(`Bildirim başarıyla '${newStatus}' durumuna getirildi.`);
        loadAdminData();
    } catch (err) {
        showToast('Hata: ' + err.message, true);
    }
}

// ================= Load User Data =================
async function loadUserData() {
    try {
        const list = await apiCall('/api/notifications');
        const tbody = document.getElementById('myNotificationsTable');

        // Metrics for user
        let myTotal = 0;
        let pending = 0;
        (list || []).forEach(n => {
            myTotal += Number(n.amount) || 0;
            if ((n.status || '').toLowerCase().includes('bekli')) pending++;
        });

        document.getElementById('metricPendingCount').textContent = pending;
        document.getElementById('metricTotalDues').textContent = formatMoney(myTotal);

        if (!list || list.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" class="empty-state">Daha önce gönderdiğiniz bir ödeme bildirimi bulunmuyor.</td></tr>`;
            return;
        }

        tbody.innerHTML = list.map(n => `
            <tr>
                <td>${formatDate(n.notificationDate)}</td>
                <td>${n.description}</td>
                <td><strong>${formatMoney(n.amount)}</strong></td>
                <td>${renderStatusBadge(n.status)}</td>
            </tr>
        `).join('');
    } catch (err) {
        console.error('Kullanıcı verisi alınamadı:', err);
    }

    // --- Aidat Çizelgesi (salt okunur) ---
    try {
        const dues = await apiCall('/api/dues');
        const duesTbody = document.getElementById('userDuesTable');
        if (!dues || dues.length === 0) {
            duesTbody.innerHTML = `<tr><td colspan="4" class="empty-state">Aidat çizelgesi henüz oluşturulmamış.</td></tr>`;
        } else {
            duesTbody.innerHTML = dues.map(d => `
                <tr>
                    <td><strong>${d.apartmentNo}</strong></td>
                    <td><strong>${formatMoney(d.amount)}</strong></td>
                    <td>${formatDate(d.dueDate)}</td>
                    <td>${d.status === 'PAID' ? '<span class="badge badge-approved">Ödendi</span>' : '<span class="badge badge-pending">Bekliyor</span>'}</td>
                </tr>
            `).join('');
            // Toplam aidat metriği
            let totalDues = dues.reduce((sum, d) => sum + (Number(d.amount) || 0), 0);
            document.getElementById('metricTotalDues').textContent = formatMoney(totalDues);
        }
    } catch (err) {
        console.error('Aidat verisi alınamadı:', err);
    }

    // --- Giderler (salt okunur) ---
    try {
        const expenses = await apiCall('/api/expenses');
        const expTbody = document.getElementById('userExpensesTable');
        if (!expenses || expenses.length === 0) {
            expTbody.innerHTML = `<tr><td colspan="4" class="empty-state">Kayıtlı gider bulunmuyor.</td></tr>`;
        } else {
            expTbody.innerHTML = expenses.map(e => `
                <tr>
                    <td><strong>${e.category}</strong></td>
                    <td>${e.description}</td>
                    <td><strong style="color: var(--danger);">${formatMoney(e.amount)}</strong></td>
                    <td>${formatDate(e.expenseDate)}</td>
                </tr>
            `).join('');
            let totalExp = expenses.reduce((sum, e) => sum + (Number(e.amount) || 0), 0);
            document.getElementById('metricTotalExpenses').textContent = formatMoney(totalExp);
        }
    } catch (err) {
        console.error('Gider verisi alınamadı:', err);
    }
}

// ================= Event Listeners =================
document.addEventListener('DOMContentLoaded', () => {
    // 1. Login Form
    document.getElementById('loginForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target));
        try {
            currentUser = await apiCall('/api/auth/login', { method: 'POST', body: JSON.stringify(data) });
            localStorage.setItem('ays_user', JSON.stringify(currentUser));
            setCookie('ays_token', currentUser.token, 7);
            showToast('Giriş başarılı.');
            renderApp();
        } catch (err) {
            showToast('Giriş yapılamadı: ' + err.message, true);
        }
    });

    // 2. Register Form
    document.getElementById('registerForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target));
        try {
            currentUser = await apiCall('/api/auth/register', { method: 'POST', body: JSON.stringify(data) });
            localStorage.setItem('ays_user', JSON.stringify(currentUser));
            setCookie('ays_token', currentUser.token, 7);
            showToast('Hesap başarıyla oluşturuldu.');
            renderApp();
        } catch (err) {
            showToast('Kayıt yapılamadı: ' + err.message, true);
        }
    });

    // 3. Logout
    document.getElementById('logoutButton').addEventListener('click', () => {
        currentUser = null;
        localStorage.removeItem('ays_user');
        eraseCookie('ays_token');
        showToast('Çıkış yapıldı.');
        renderApp();
    });

    // 4. Resident: Payment Notification Submit
    document.getElementById('paymentNotificationForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target));
        try {
            await apiCall('/api/notifications', { method: 'POST', body: JSON.stringify(data) });
            showToast('Ödeme bildiriminiz yöneticiye iletildi.');
            e.target.reset();
            loadUserData();
        } catch (err) {
            showToast('Bildirim gönderilemedi: ' + err.message, true);
        }
    });

    // 5. Admin: Bank Account Create
    const bankForm = document.getElementById('bankAccountForm');
    if (bankForm) {
        bankForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = Object.fromEntries(new FormData(e.target));
            try {
                await apiCall('/api/bank-accounts', { method: 'POST', body: JSON.stringify(data) });
                showToast('Banka hesabı kaydedildi.');
                e.target.reset();
                loadAdminData();
            } catch (err) {
                showToast('Hata: ' + err.message, true);
            }
        });
    }

    // 6. Admin: Dues Create
    const duesForm = document.getElementById('duesForm');
    if (duesForm) {
        duesForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = Object.fromEntries(new FormData(e.target));
            try {
                await apiCall('/api/dues', { method: 'POST', body: JSON.stringify(data) });
                showToast('Aidat kaydı oluşturuldu.');
                e.target.reset();
                loadAdminData();
            } catch (err) {
                showToast('Hata: ' + err.message, true);
            }
        });
    }

    // 7. Admin: Expense Create
    const expenseForm = document.getElementById('expenseForm');
    if (expenseForm) {
        expenseForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const data = Object.fromEntries(new FormData(e.target));
            try {
                await apiCall('/api/expenses', { method: 'POST', body: JSON.stringify(data) });
                showToast('Gider başarıyla düşüldü.');
                e.target.reset();
                loadAdminData();
            } catch (err) {
                showToast('Hata: ' + err.message, true);
            }
        });
    }

    // Auto-restore session from localStorage if available
    const saved = localStorage.getItem('ays_user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            setCookie('ays_token', currentUser.token, 7);
        } catch (_) {
            localStorage.removeItem('ays_user');
        }
    }

    renderApp();
});
