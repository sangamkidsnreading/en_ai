// Dashboard Manager Class
class DashboardManager {
    constructor() {
        this.baseUrl = '/api/dashboard';
        this.currentData = null;
        this.init();
    }

    init() {
        console.log('Dashboard Manager 초기화 중...');
        this.loadDashboardData();
       // this.loadTodayProgress();
        this.loadBadgesData();
        this.loadRankingsData();
        this.loadLevelProgress();
        this.loadDailyGoals();
        
        // 달력 초기화
        this.initializeCalendar();
        
        this.setupEventListeners();
    }

    // 오늘의 진행도 로드
    async loadTodayProgress() {
        try {
            console.log('오늘의 진행도 로드 중...');
            const response = await fetch(`${this.baseUrl}/today-progress`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
           // this.updateTodayProgressUI(data);
            console.log('오늘의 진행도 로드 완료:', data);
        } catch (error) {
            console.error('오늘의 진행도 로드 실패:', error);
        }
    }

    
    async loadDashboardData() {
        try {
            const response = await fetch('/api/dashboard/stats');
            if (!response.ok) throw new Error('대시보드 통계 로드 실패');
            const stats = await response.json();
    
            // 누적 단어
            document.getElementById('dashboard-words-learned').textContent = stats.wordsLearned ?? 0;
            // 누적 문장
            document.getElementById('dashboard-sentences-learned').textContent = stats.sentencesLearned ?? 0;
            // 누적 코인
            document.getElementById('dashboard-total-coins').textContent = stats.totalCoins ?? 0;
            // 연속 학습일
            document.getElementById('dashboard-streak-days').textContent = stats.streakDays ?? 1;
            document.getElementById('dashboard-streak-duration').textContent = (stats.streakDays ?? 1) + ' 일';
    
            // 변화율(%) 계산 및 표시 (예시: 전일 대비 증가율)
            if ('previousWordsLearned' in stats) {
                const diff = stats.wordsLearned - stats.previousWordsLearned;
                const percent = stats.previousWordsLearned > 0 ? Math.round((diff / stats.previousWordsLearned) * 100) : 0;
                document.getElementById('dashboard-words-change').textContent = (percent >= 0 ? '+' : '') + percent + '%';
                document.getElementById('dashboard-words-change').className = 'stat-change ' + (percent >= 0 ? 'positive' : 'negative');
            }
            if ('previousSentencesLearned' in stats) {
                const diff = stats.sentencesLearned - stats.previousSentencesLearned;
                const percent = stats.previousSentencesLearned > 0 ? Math.round((diff / stats.previousSentencesLearned) * 100) : 0;
                document.getElementById('dashboard-sentences-change').textContent = (percent >= 0 ? '+' : '') + percent + '%';
                document.getElementById('dashboard-sentences-change').className = 'stat-change ' + (percent >= 0 ? 'positive' : 'negative');
            }
            if ('previousTotalCoins' in stats) {
                const diff = stats.totalCoins - stats.previousTotalCoins;
                const percent = stats.previousTotalCoins > 0 ? Math.round((diff / stats.previousTotalCoins) * 100) : 0;
                document.getElementById('dashboard-coins-change').textContent = (percent >= 0 ? '+' : '') + percent + '%';
                document.getElementById('dashboard-coins-change').className = 'stat-change ' + (percent >= 0 ? 'positive' : 'negative');
            }
        } catch (error) {
            console.error('대시보드 통계 로드 실패:', error);
        }
    }

    // 월간 달력 데이터 로드
    async loadCalendarData(year, month) {
        try {
            console.log(`달력 데이터 로드 중... (${year}-${month})`);
            const response = await fetch(`${this.baseUrl}/calendar/${year}/${month}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateCalendarUI(data);
            console.log('달력 데이터 로드 완료:', data);
        } catch (error) {
            console.error('달력 데이터 로드 실패:', error);
        }
    }

    // 뱃지 데이터 로드
    async loadBadgesData() {
        try {
            console.log('뱃지 데이터 로드 중...');
            const response = await fetch(`${this.baseUrl}/badges`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateBadgesUI(data);
            console.log('뱃지 데이터 로드 완료:', data);
        } catch (error) {
            console.error('뱃지 데이터 로드 실패:', error);
        }
    }

    // 랭킹 데이터 로드
    async loadRankingsData() {
        try {
            console.log('랭킹 데이터 로드 중...');
            const response = await fetch(`${this.baseUrl}/rankings`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateRankingsUI(data);
            console.log('랭킹 데이터 로드 완료:', data);
        } catch (error) {
            console.error('랭킹 데이터 로드 실패:', error);
        }
    }

    // 레벨 진행도 로드
    async loadLevelProgress() {
        try {
            console.log('레벨 진행도 로드 중...');
            const response = await fetch(`${this.baseUrl}/level-progress`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateLevelProgressUI(data);
            console.log('레벨 진행도 로드 완료:', data);
        } catch (error) {
            console.error('레벨 진행도 로드 실패:', error);
        }
    }

    // 일일 목표 로드
    async loadDailyGoals() {
        try {
            console.log('일일 목표 로드 중...');
            const response = await fetch(`${this.baseUrl}/daily-goals`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateDailyGoalsUI(data);
            console.log('일일 목표 로드 완료:', data);
        } catch (error) {
            console.error('일일 목표 로드 실패:', error);
        }
    }

    // 오늘의 진행도 UI 업데이트
    updateTodayProgressUI(data) {
        // 오늘 학습한 단어/문장 수 표시
        const todayWords = data.words ? data.words.length : 0;
        const todaySentences = data.sentences ? data.sentences.length : 0;
        const todayCoins = data.coin || 0;

        // 오늘의 진행도가 있으면 표시 업데이트
        if (todayWords > 0 || todaySentences > 0) {
            this.updateElement('dashboard-words-learned', todayWords);
            this.updateElement('dashboard-sentences-learned', todaySentences);
            this.updateElement('dashboard-total-coins', todayCoins);
        }
    }

    // 달력 UI 업데이트
    updateCalendarUI(data) {
        const calendarData = data.calendarData || {};
        const calendarDays = document.querySelectorAll('.day-cell');
        
        calendarDays.forEach((dayCell, index) => {
            const dayNumber = dayCell.querySelector('.day-number');
            if (dayNumber) {
                const day = parseInt(dayNumber.textContent);
                if (day && calendarData[`${data.currentYear}-${String(data.currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`]) {
                    const dayData = calendarData[`${data.currentYear}-${String(data.currentMonth).padStart(2, '0')}-${String(day).padStart(2, '0')}`];
                    this.updateCalendarDayStatus(dayCell, dayData.status);
                }
            }
        });
    }

    // 달력 생성 및 초기화
    initializeCalendar() {
        const calendarDaysContainer = document.querySelector('.calendar-days');
        if (!calendarDaysContainer) return;

        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;
        const currentDay = now.getDate();

        // 달력 헤더 업데이트
        this.updateCalendarHeader(currentYear, currentMonth);

        // 달력 그리드 생성
        this.generateCalendarGrid(calendarDaysContainer, currentYear, currentMonth, currentDay);

        // 현재 월의 달력 데이터 로드
        this.loadCalendarData(currentYear, currentMonth);
    }

    // 달력 헤더 업데이트
    updateCalendarHeader(year, month) {
        const monthNames = ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'];
        const calendarTitle = document.querySelector('.calendar-title');
        if (calendarTitle) {
            calendarTitle.innerHTML = `
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2" stroke="currentColor" stroke-width="2"></rect>
                    <line x1="16" y1="2" x2="16" y2="6" stroke="currentColor" stroke-width="2"></line>
                    <line x1="8" y1="2" x2="8" y2="6" stroke="currentColor" stroke-width="2"></line>
                    <line x1="3" y1="10" x2="21" y2="10" stroke="currentColor" stroke-width="2"></line>
                </svg>
                ${year}년 ${monthNames[month - 1]} 학습 달력
            `;
        }
    }

    // 달력 그리드 생성
    generateCalendarGrid(container, year, month, currentDay) {
        const firstDay = new Date(year, month - 1, 1);
        const lastDay = new Date(year, month, 0);
        const startDate = new Date(firstDay);
        const endDate = new Date(lastDay);

        // 첫 번째 주의 시작일을 일요일로 맞추기
        const dayOfWeek = firstDay.getDay();
        startDate.setDate(startDate.getDate() - dayOfWeek);

        let html = '';
        let currentDate = new Date(startDate);

        // 6주치 달력 생성 (최대 42일)
        for (let week = 0; week < 6; week++) {
            for (let day = 0; day < 7; day++) {
                const date = new Date(currentDate);
                const dayNumber = date.getDate();
                const isCurrentMonth = date.getMonth() === month - 1;
                const isToday = date.getDate() === currentDay && date.getMonth() === month - 1 && date.getFullYear() === year;
                const isPast = date < new Date(year, month - 1, currentDay);
                const isFuture = date > new Date(year, month - 1, currentDay);

                let cellClass = 'day-cell';
                let statusText = '';

                if (!isCurrentMonth) {
                    cellClass += ' other-month';
                } else if (isToday) {
                    cellClass += ' current-day';
                    statusText = '오늘';
                } else if (isPast) {
                    cellClass += ' past';
                    statusText = '지난날';
                } else if (isFuture) {
                    cellClass += ' upcoming';
                    statusText = '다음날';
                }

                // 기본 상태 클래스 추가
                if (isCurrentMonth) {
                    cellClass += ' not-started';
                }

                html += `
                    <div class="${cellClass}" data-date="${date.toISOString().split('T')[0]}">
                        <div class="day-number">${dayNumber}</div>
                        ${statusText ? `<div class="day-status">${statusText}</div>` : ''}
                    </div>
                `;

                currentDate.setDate(currentDate.getDate() + 1);
            }
        }

        container.innerHTML = html;

        // 날짜별 클릭 이벤트 추가
        this.addCalendarDayEvents();
    }

    // 달력 날짜 클릭 이벤트 추가
    addCalendarDayEvents() {
        const dayCells = document.querySelectorAll('.day-cell');
        dayCells.forEach(cell => {
            cell.addEventListener('click', () => {
                const date = cell.dataset.date;
                if (date) {
                    this.showDayDetails(date);
                }
            });
        });
    }

    // 날짜 상세 정보 표시
    async showDayDetails(date) {
        try {
            console.log(`날짜 상세 정보 조회 중: ${date}`);
            const response = await fetch(`${this.baseUrl}/stats/date?date=${date}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const dayData = await response.json();
            
            if (dayData.completedWords > 0 || dayData.completedSentences > 0 || dayData.coinsEarned > 0) {
                this.showToast('학습 기록', 
                    `${date}: 단어 ${dayData.completedWords}개, 문장 ${dayData.completedSentences}개, 코인 ${dayData.coinsEarned}개`);
            } else {
                this.showToast('학습 기록', `${date}: 아직 학습 기록이 없습니다.`);
            }
            
            console.log('날짜별 통계 데이터:', dayData);
        } catch (error) {
            console.error('날짜별 통계 조회 실패:', error);
            this.showToast('학습 기록', `${date}: 데이터를 불러올 수 없습니다.`);
        }
    }

    // 날짜별 데이터 조회 (기존 메서드 - API 호출로 대체됨)
    getDayData(date) {
        // 이 메서드는 더 이상 사용되지 않음 (showDayDetails에서 직접 API 호출)
        // 호환성을 위해 남겨둠
        const today = new Date().toISOString().split('T')[0];
        if (date === today) {
            return {
                wordsCompleted: this.currentData?.wordsLearned || 0,
                sentencesCompleted: this.currentData?.sentencesLearned || 0,
                coinsEarned: this.currentData?.totalCoins || 0
            };
        }
        return null;
    }

    // 토스트 알림 표시
    showToast(title, description) {
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.innerHTML = `
            <div class="toast-title">${title}</div>
            <div class="toast-description">${description}</div>
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    // 뱃지 UI 업데이트
    updateBadgesUI(data) {
        const badges = data.badges || [];
        const badgeGrid = document.querySelector('.badge-grid');
        
        if (badgeGrid) {
            badgeGrid.innerHTML = '';
            badges.forEach(badge => {
                const badgeItem = document.createElement('div');
                badgeItem.className = `badge-item ${badge.isEarned ? 'earned' : ''}`;
                badgeItem.innerHTML = `
                    <div class="badge-icon">${badge.icon}</div>
                    <div class="badge-name">${badge.name}</div>
                `;
                badgeGrid.appendChild(badgeItem);
            });
        }

        this.updateElement('dashboard-badge-count', `${data.earnedBadges || 0}/${data.totalBadges || 0}`);
    }

    // 랭킹 UI 업데이트
    updateRankingsUI(data) {
        const rankings = data.rankings || [];
        const rankingList = document.querySelector('.ranking-list');
        
        if (rankingList) {
            rankingList.innerHTML = '';
            rankings.forEach(ranking => {
                const rankItem = document.createElement('div');
                rankItem.className = `rank-item rank-${ranking.rank}`;
                rankItem.innerHTML = `
                    <div class="rank-position">${ranking.rank}</div>
                    <div class="rank-info">
                        <div class="rank-name">${ranking.name}</div>
                        <div class="rank-details">단어 ${ranking.wordsLearned}개 · 문장 ${ranking.sentencesLearned}개</div>
                    </div>
                    <div class="rank-badge">${ranking.badge}</div>
                    <div class="rank-score">⭐ ${ranking.rank}위</div>
                `;
                rankingList.appendChild(rankItem);
            });
        }
    }

    // 레벨 진행도 UI 업데이트
    updateLevelProgressUI(data) {
        this.updateElement('dashboard-current-level', `Level ${data.currentLevel || 1}`);
        this.updateElement('dashboard-level-progress', `${data.levelProgress || 0}%`);
        this.updateElement('dashboard-level-description', `다음 레벨까지 ${data.wordsToNextLevel || 100}단어`);
    }

    // 일일 목표 UI 업데이트
    updateDailyGoalsUI(data) {
        // 일일 목표 진행도 표시 (필요시 추가 구현)
        console.log('일일 목표 데이터:', data);
    }

    // 달력 날짜 상태 업데이트
    updateCalendarDayStatus(dayCell, status) {
        // 기존 상태 클래스 제거
        dayCell.classList.remove('completed', 'partial', 'not-started', 'current');
        
        // 새로운 상태 클래스 추가
        switch (status) {
            case 'completed':
                dayCell.classList.add('completed');
                break;
            case 'partial':
                dayCell.classList.add('partial');
                break;
            case 'current':
                dayCell.classList.add('current');
                break;
            default:
                dayCell.classList.add('not-started');
        }
    }

    // 요소 업데이트 헬퍼 함수
    updateElement(id, value) {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value;
        }
    }

    // 애니메이션 효과 추가
    addAnimationEffects() {
        const statNumbers = document.querySelectorAll('.stat-number');
        statNumbers.forEach(number => {
            number.style.animation = 'fadeInUp 0.6s ease-out';
        });
    }

    // 기본 데이터 반환
    getDefaultData() {
        return {
            wordsLearned: 0,
            sentencesLearned: 0,
            totalCoins: 100,
            streakDays: 1,
            currentLevel: 1,
            levelProgress: 0,
            wordsToNextLevel: 100,
            completionRate: 0.0,
            totalWords: 0,
            totalSentences: 0,
            dailyWordGoal: 10,
            dailySentenceGoal: 5,
            dailyWordProgress: 0,
            dailySentenceProgress: 0,
            previousWordsLearned: 0,
            previousSentencesLearned: 0,
            previousTotalCoins: 0
        };
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 새로고침 버튼 (필요시 추가)
        const refreshButton = document.querySelector('.dashboard-refresh');
        if (refreshButton) {
            refreshButton.addEventListener('click', () => {
                this.loadDashboardData();
            });
        }

        // 자동 새로고침 (5분마다)
        setInterval(() => {
            this.loadDashboardData();
        }, 5 * 60 * 1000);
    }

    // 전체 대시보드 새로고침
    refreshDashboard() {
        console.log('대시보드 전체 새로고침 중...');
        this.loadDashboardData();
        this.loadTodayProgress();
        this.loadBadgesData();
        this.loadRankingsData();
        this.loadLevelProgress();
        this.loadDailyGoals();
        
        // 현재 월의 달력 데이터 로드
        const now = new Date();
        this.loadCalendarData(now.getFullYear(), now.getMonth() + 1);
    }

    // 퍼센트 변화량 계산 함수
    calculatePercentageChange(current, previous) {
        if (previous === 0) {
            return current > 0 ? 100 : 0;
        }
        return Math.round(((current - previous) / previous) * 100);
    }
}

// 전역 DashboardManager 인스턴스
let dashboardManager;

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard DOM 로드 완료');
    // 대시보드 페이지가 활성화되어 있을 때만 초기화
    const dashboardPage = document.getElementById('dashboard-page');
    if (dashboardPage && dashboardPage.classList.contains('active')) {
        console.log('대시보드 페이지가 활성화됨 - DashboardManager 초기화');
        dashboardManager = new DashboardManager();
    }
});

// 페이지 전환 시 대시보드 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 페이지 전환 감지
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                const dashboardPage = document.getElementById('dashboard-page');
                if (dashboardPage && dashboardPage.classList.contains('active')) {
                    if (!dashboardManager) {
                        console.log('대시보드 페이지로 전환됨 - DashboardManager 초기화');
                        dashboardManager = new DashboardManager();
                    } else {
                        console.log('대시보드 페이지로 전환됨 - 데이터 새로고침');
                        dashboardManager.refreshDashboard();
                    }
                }
            }
        });
    });

    const dashboardPage = document.getElementById('dashboard-page');
    if (dashboardPage) {
        observer.observe(dashboardPage, { attributes: true });
    }
});

// 페이지가 보일 때마다 데이터 새로고침
document.addEventListener('visibilitychange', function() {
    if (!document.hidden && dashboardManager) {
        console.log('페이지가 다시 보임 - 대시보드 새로고침');
        dashboardManager.refreshDashboard();
    }
});

// 전역 함수로 노출 (다른 스크립트에서 사용 가능)
window.DashboardManager = DashboardManager;
window.dashboardManager = dashboardManager;
