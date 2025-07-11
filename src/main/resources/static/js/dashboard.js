// 대시보드 통계 fetch 및 UI 반영
async function fetchDashboardStats() {
    try {
        const res = await fetch('/dashboard/api/stats', { credentials: 'include' });
        if (!res.ok) throw new Error('대시보드 통계 API 오류');
        const data = await res.json();
        // 단어, 문장, 코인, 연속일수 DOM 업데이트
        document.querySelectorAll('.stat-card .stat-label').forEach((el, idx) => {
            if (el.textContent.includes('단어')) {
                el.parentElement.querySelector('.stat-number').textContent = data.wordCount;
            } else if (el.textContent.includes('문장')) {
                el.parentElement.querySelector('.stat-number').textContent = data.sentenceCount;
            } else if (el.textContent.includes('코인')) {
                el.parentElement.querySelector('.stat-number').textContent = data.coinCount;
            } else if (el.textContent.includes('연속')) {
                el.parentElement.querySelector('.stat-number').textContent = data.streak;
                el.parentElement.querySelector('.stat-duration').textContent = data.streak + ' 일';
            }
        });
    } catch (e) {
        console.warn('대시보드 통계 갱신 실패:', e);
    }
}

async function fetchDashboardCalendar(year, month) {
    try {
        const res = await fetch(`/dashboard/api/calendar?year=${year}&month=${month}`, { credentials: 'include' });
        if (!res.ok) throw new Error('달력 API 오류');
        const data = await res.json();
        // data: [{date: '2024-07-01', completed: true, ...}, ...]
        const todayStr = new Date().toISOString().slice(0, 10);
        document.querySelectorAll('.calendar-days .day-cell').forEach((cell, idx) => {
            const dayNumEl = cell.querySelector('.day-number');
            if (!dayNumEl) return;
            const day = parseInt(dayNumEl.textContent, 10);
            const found = data.find(d => new Date(d.date).getDate() === day);
            cell.classList.remove('completed', 'current-day', 'upcoming');
            if (found) {
                if (found.completed) {
                    cell.classList.add('completed');
                }
                // 오늘 표시
                if (found.date === todayStr) {
                    cell.classList.add('current-day');
                    const status = cell.querySelector('.day-status');
                    if (status) status.textContent = '오늘';
                }
            }
        });
    } catch (e) {
        console.warn('달력 데이터 갱신 실패:', e);
    }
}

window.addEventListener('DOMContentLoaded', () => {
    fetchDashboardStats();
    const now = new Date();
    fetchDashboardCalendar(now.getFullYear(), now.getMonth() + 1);
});
