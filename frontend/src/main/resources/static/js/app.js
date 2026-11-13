/* Общий хелпер */
async function api(gateway, path, options = {}) {
    const res = await fetch(gateway + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    if (!res.ok) {
        let msg = res.statusText;
        try { msg = (await res.json()).error || msg; } catch (e) {}
        throw new Error(msg);
    }
    return res.status === 204 ? null : res.json();
}

const fmtDate = iso => iso ? new Date(iso).toLocaleString('ru-RU') : '—';
const isoToRu = d => {  // yyyy-MM-dd -> dd.MM.yyyy (формат @JsonFormat Java-DTO)
    const [y, m, day] = d.split('-');
    return `${day}.${m}.${y}`;
};

/* ---------------- Клиенты ---------------- */
const ClientsPage = {
    gateway: '', page: 0, size: 10, totalPages: 1,

    init(gateway) {
        this.gateway = gateway;
        document.getElementById('clientForm').addEventListener('submit', e => this.create(e));
        document.getElementById('applyFilter').addEventListener('click', () => { this.page = 0; this.load(); });
        document.getElementById('prevPage').addEventListener('click', () => { if (this.page > 0) { this.page--; this.load(); } });
        document.getElementById('nextPage').addEventListener('click', () => { if (this.page < this.totalPages - 1) { this.page++; this.load(); } });
        this.load();
    },

    async load() {
        const lastName = document.getElementById('searchLastName').value;
        let path = `/api/v1/clients?page=${this.page}&size=${this.size}`;
        if (lastName) path += `&lastName=${encodeURIComponent(lastName)}`;
        try {
            const data = await api(this.gateway, path);
            this.totalPages = data.totalPages || 1;
            document.getElementById('pageInfo').textContent = `Стр. ${data.page + 1} из ${this.totalPages} (всего ${data.totalElements})`;
            const tbody = document.querySelector('#clientsTable tbody');
            tbody.innerHTML = '';
            (data.content || []).forEach(c => {
                const tr = tbody.insertRow();
                tr.innerHTML = `<td>${c.lastName}</td><td>${c.firstName}</td><td>${c.surname}</td>
                                <td>${c.phoneNumber}</td><td>${c.email}</td><td>${c.birthDate || ''}</td>`;
            });
        } catch (e) { alert('Ошибка загрузки клиентов: ' + e.message); }
    },

    async create(e) {
        e.preventDefault();
        const f = e.target;
        const body = {
            firstName: f.firstName.value, lastName: f.lastName.value, surname: f.surname.value,
            phoneNumber: f.phoneNumber.value, email: f.email.value,
            birthDate: isoToRu(f.birthDate.value)
        };
        try {
            await api(this.gateway, '/api/v1/clients', { method: 'POST', body: JSON.stringify(body) });
            document.getElementById('clientMsg').textContent = 'Клиент создан';
            f.reset(); this.page = 0; this.load();
        } catch (err) { document.getElementById('clientMsg').textContent = 'Ошибка: ' + err.message; }
    }
};

/* ---------------- Посещения ---------------- */
const VisitsPage = {
    gateway: '', page: 0, size: 10, totalPages: 1,

    init(gateway) {
        this.gateway = gateway;
        document.getElementById('checkInForm').addEventListener('submit', e => this.checkIn(e));
        document.getElementById('prevPage').addEventListener('click', () => { if (this.page > 0) { this.page--; this.load(); } });
        document.getElementById('nextPage').addEventListener('click', () => { if (this.page < this.totalPages - 1) { this.page++; this.load(); } });
        this.load();
    },

    async load() {
        try {
            const data = await api(this.gateway, `/api/v1/visits?page=${this.page}&size=${this.size}`);
            this.totalPages = data.totalPages || 1;
            document.getElementById('pageInfo').textContent = `Стр. ${data.page + 1} из ${this.totalPages} (всего ${data.totalElements})`;
            const tbody = document.querySelector('#visitsTable tbody');
            tbody.innerHTML = '';
            (data.content || []).forEach(v => {
                const name = v.clientDTO ? `${v.clientDTO.lastName} ${v.clientDTO.firstName}` : '—';
                const sub = v.subscriptionDTO ? v.subscriptionDTO.subscriptionType : '—';
                const tr = tbody.insertRow();
                tr.innerHTML = `<td>${name}</td><td>${sub}</td><td>${fmtDate(v.checkInTime)}</td><td>${fmtDate(v.checkOutTime)}</td>`;
                if (!v.checkOutTime) {
                    const btn = document.createElement('button');
                    btn.textContent = 'Check-out';
                    btn.addEventListener('click', () => this.checkOut(v.id ?? ''));
                    tr.insertCell().appendChild(btn);
                } else {
                    tr.insertCell();
                }
            });
        } catch (e) { alert('Ошибка загрузки посещений: ' + e.message); }
    },

    async checkIn(e) {
        e.preventDefault();
        const f = e.target;
        const body = {
            clientDTO: {
                firstName: f.firstName.value, lastName: f.lastName.value, surname: f.surname.value,
                phoneNumber: f.phoneNumber.value,
                email: f.email.value || undefined,
                birthDate: f.birthDate.value ? isoToRu(f.birthDate.value) : undefined
            },
            subscriptionDTO: {
                subscriptionType: f.subscriptionType.value,
                status: 'ACTIVE',
                expire: f.expire.value ? isoToRu(f.expire.value) : undefined,
                createdAt: isoToRu(new Date().toISOString().slice(0, 10))
            },
            checkInTime: new Date().toISOString().slice(0, 19)
        };
        try {
            await api(this.gateway, '/api/v1/visits/check-in', { method: 'POST', body: JSON.stringify(body) });
            document.getElementById('checkInMsg').textContent = 'Вход зарегистрирован';
            f.reset(); this.page = 0; this.load();
        } catch (err) { document.getElementById('checkInMsg').textContent = 'Ошибка: ' + err.message; }
    },

    async checkOut(visitId) {
        try {
            await api(this.gateway,
                `/api/v1/visits/${visitId}/check-out?checkOutTime=${new Date().toISOString().slice(0, 19)}`,
                { method: 'POST' });
            this.load();
        } catch (e) { alert('Ошибка check-out: ' + e.message); }
    }
};

/* ---------------- Отчёты ---------------- */
const ReportsPage = {
    gateway: '',

    init(gateway) {
        this.gateway = gateway;
        document.getElementById('reportForm').addEventListener('submit', e => this.generate(e));
        document.getElementById('refreshReports').addEventListener('click', () => this.load());
        this.load();
    },

    async load() {
        try {
            const data = await api(this.gateway, '/api/v1/reports');
            const tbody = document.querySelector('#reportsTable tbody');
            tbody.innerHTML = '';
            (data || []).forEach(r => {
                const tr = tbody.insertRow();
                const period = `${r.period_start} – ${r.period_end}`;
                tr.innerHTML = `<td>${r.id}</td><td>${period}</td><td>${fmtDate(r.generated_at)}</td><td>${r.generated_by || ''}</td>`;
                const a = document.createElement('a');
                a.href = `${this.gateway}/api/v1/reports/${r.id}/download`;
                a.textContent = 'Скачать .txt';
                a.className = 'btn-link';
                tr.insertCell().appendChild(a);
            });
        } catch (e) { alert('Ошибка загрузки отчётов: ' + e.message); }
    },

    async generate(e) {
        e.preventDefault();
        const f = e.target;
        const from = f.from.value, to = f.to.value;
        if (from > to) { document.getElementById('reportMsg').textContent = '`from` должен быть <= `to`'; return; }
        try {
            const resp = await api(this.gateway,
                `/api/v1/reports/generate?from=${from}&to=${to}`,
                { method: 'POST', body: JSON.stringify({ generated_by: 'MANUAL' }) });
            document.getElementById('reportMsg').textContent = `Отчёт #${resp.report_id} создан: ${resp.file_path}`;
            this.load();
        } catch (err) { document.getElementById('reportMsg').textContent = 'Ошибка: ' + err.message; }
    }
};
