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
        this.loadLearningGraph();
       // this.loadTodayProgress();
        this.loadBadgesData();
        this.loadRankingsData();
        this.loadLevelProgress();
        this.loadStreakInfo();
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
            console.log('대시보드 통계 데이터 로드 시작...');
            
            // 대시보드 통계와 코인 정보를 병렬로 로드
            const [statsResponse, coinsResponse] = await Promise.all([
                fetch('/learning/api/dashboard/stats'),
                fetch('/api/coins/user')
            ]);
            
            if (!statsResponse.ok) {
                throw new Error(`대시보드 통계 HTTP error! status: ${statsResponse.status}`);
            }
            
            if (!coinsResponse.ok) {
                throw new Error(`코인 정보 HTTP error! status: ${coinsResponse.status}`);
            }
            
            const stats = await statsResponse.json();
            const coins = await coinsResponse.json();
            
            console.log('대시보드 통계 데이터 로드 완료:', stats);
            console.log('코인 정보 로드 완료:', coins);
            console.log('📊 상세 통계:', {
                todayWordsLearned: stats.todayWordsLearned,
                todaySentencesLearned: stats.todaySentencesLearned,
                todayCoinsEarned: coins.dailyCoins,
                streakDays: stats.streakDays,
                totalCoins: coins.totalCoins,
                wordsChangePercent: stats.wordsChangePercent,
                sentencesChangePercent: stats.sentencesChangePercent,
                coinsChangePercent: stats.coinsChangePercent
            });
    
            // 오늘 학습한 단어/문장/코인
            this.updateElement('dashboard-words-learned', stats.todayWordsLearned ?? 0);
            this.updateElement('dashboard-sentences-learned', stats.todaySentencesLearned ?? 0);
            this.updateElement('dashboard-total-coins', coins.dailyCoins ?? 0);
            this.updateElement('dashboard-streak-days', stats.streakDays ?? 1);
            this.updateElement('dashboard-streak-duration', (stats.streakDays ?? 1) + ' 일');
            this.updateElement('dashboard-total-coins-all', coins.totalCoins ?? 0);
    
            // 변화율(%) 표시
            const wordsChangeElement = document.getElementById('dashboard-words-change');
            if (wordsChangeElement) {
                wordsChangeElement.textContent = (stats.wordsChangePercent >= 0 ? '+' : '') + stats.wordsChangePercent + '%';
                wordsChangeElement.className = 'stat-change ' + (stats.wordsChangePercent >= 0 ? 'positive' : 'negative');
            }

            const sentencesChangeElement = document.getElementById('dashboard-sentences-change');
            if (sentencesChangeElement) {
                sentencesChangeElement.textContent = (stats.sentencesChangePercent >= 0 ? '+' : '') + stats.sentencesChangePercent + '%';
                sentencesChangeElement.className = 'stat-change ' + (stats.sentencesChangePercent >= 0 ? 'positive' : 'negative');
            }

            const coinsChangeElement = document.getElementById('dashboard-coins-change');
            if (coinsChangeElement) {
                coinsChangeElement.textContent = (stats.coinsChangePercent >= 0 ? '+' : '') + stats.coinsChangePercent + '%';
                coinsChangeElement.className = 'stat-change ' + (stats.coinsChangePercent >= 0 ? 'positive' : 'negative');
            }
            
            console.log('대시보드 통계 UI 업데이트 완료');
        } catch (error) {
            console.error('대시보드 통계 로드 실패:', error);
            // 에러 시 기본값 설정
            this.updateElement('dashboard-words-learned', 0);
            this.updateElement('dashboard-sentences-learned', 0);
            this.updateElement('dashboard-total-coins', 0);
            this.updateElement('dashboard-streak-days', 1);
            this.updateElement('dashboard-streak-duration', '1 일');
            this.updateElement('dashboard-total-coins-all', 0);
            
            // 코인 정보만 별도로 시도
            try {
                const coinsResponse = await fetch('/api/coins/user');
                if (coinsResponse.ok) {
                    const coins = await coinsResponse.json();
                    this.updateElement('dashboard-total-coins', coins.dailyCoins ?? 0);
                    this.updateElement('dashboard-total-coins-all', coins.totalCoins ?? 0);
                    console.log('코인 정보만 로드 성공:', coins);
                }
            } catch (coinError) {
                console.error('코인 정보 로드도 실패:', coinError);
            }
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

    // 연속 학습일 정보 로드
    async loadStreakInfo() {
        try {
            console.log('연속 학습일 정보 로드 중...');
            const response = await fetch('/api/streak');
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateStreakInfoUI(data);
            console.log('연속 학습일 정보 로드 완료:', data);
        } catch (error) {
            console.error('연속 학습일 정보 로드 실패:', error);
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

    // 7일 학습량 그래프 로딩 메서드 추가
    async loadLearningGraph() {
        try {
            console.log('학습량 그래프 데이터 로드 중...');
            
            // Chart.js가 로드되었는지 확인
            if (typeof Chart === 'undefined') {
                console.error('Chart.js가 로드되지 않았습니다.');
                console.log('Chart 객체 확인:', typeof Chart);
                console.log('window.Chart 확인:', typeof window.Chart);
                
                // Chart.js를 동적으로 로드 시도
                try {
                    await this.loadChartJS();
                } catch (error) {
                    console.error('Chart.js 동적 로드 실패:', error);
                    return;
                }
                
                if (typeof Chart === 'undefined') {
                    console.error('Chart.js 동적 로드 후에도 Chart 객체가 없습니다.');
                    return;
                }
            }
            console.log('Chart.js 로드 확인됨:', typeof Chart);
            
            const response = await fetch('/learning/api/dashboard/graph');
            console.log('그래프 API 응답 상태:', response.status);
            
            if (!response.ok) throw new Error('그래프 데이터 로드 실패');
            const data = await response.json();
            console.log('그래프 데이터 로드 완료:', data);

            const canvas = document.getElementById('learningChart');
            if (!canvas) {
                console.error('learningChart 캔버스를 찾을 수 없습니다.');
                return;
            }
            console.log('캔버스 요소 찾음:', canvas);

            console.log('캔버스 크기:', canvas.width, 'x', canvas.height);
            console.log('캔버스 스타일:', canvas.style.width, 'x', canvas.style.height);
            
            const ctx = canvas.getContext('2d');
            if (!ctx) {
                console.error('캔버스 컨텍스트를 가져올 수 없습니다.');
                return;
            }
            console.log('캔버스 컨텍스트 생성됨');

            // 기존 차트가 있다면 제거 (안전하게)
            if (window.learningChart && typeof window.learningChart.destroy === 'function') {
                console.log('기존 차트 제거');
                window.learningChart.destroy();
            } else if (window.learningChart) {
                console.log('기존 차트 객체는 있지만 destroy 메서드가 없음 - 초기화');
                window.learningChart = null;
            }

            console.log('새 차트 생성 시작...');
            console.log('데이터 확인:', {
                labels: data.labels,
                wordsData: data.wordsData,
                sentencesData: data.sentencesData
            });

            // 테스트용: 모든 데이터가 0이면 샘플 데이터 사용
            let wordsData = data.wordsData;
            let sentencesData = data.sentencesData;
            
            if (wordsData.every(val => val === 0) && sentencesData.every(val => val === 0)) {
                console.log('모든 데이터가 0이므로 테스트 데이터 사용');
                wordsData = [2, 3, 1, 4, 2, 3, 1];
                sentencesData = [1, 2, 1, 3, 1, 2, 1];
            }

            // Chart 객체가 제대로 로드되었는지 한 번 더 확인
            if (typeof Chart === 'undefined') {
                console.error('Chart 객체가 정의되지 않았습니다.');
                return;
            }
            
            console.log('Chart 객체 확인:', typeof Chart);
            console.log('Chart 생성자 확인:', typeof Chart.prototype);
            
            window.learningChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data.labels,
                    datasets: [
                        {
                            label: '단어',
                            data: wordsData,
                            backgroundColor: '#8b5cf6',
                            borderColor: '#8b5cf6',
                            borderWidth: 1
                        },
                        {
                            label: '문장',
                            data: sentencesData,
                            backgroundColor: '#eab308',
                            borderColor: '#eab308',
                            borderWidth: 1
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { 
                            position: 'top',
                            labels: {
                                usePointStyle: true,
                                padding: 20
                            }
                        }
                    },
                    scales: {
                        y: { 
                            beginAtZero: true,
                            ticks: {
                                stepSize: 1
                            }
                        },
                        x: {
                            ticks: {
                                maxRotation: 45
                            }
                        }
                    }
                }
            });
            console.log('그래프 생성 완료');
            
            // 그래프가 실제로 렌더링되었는지 확인
            setTimeout(() => {
                console.log('그래프 렌더링 확인:', window.learningChart);
                console.log('캔버스 부모 요소:', canvas.parentElement);
                console.log('그래프 섹션:', document.querySelector('.learning-graph-section'));
            }, 1000);
            
        } catch (error) {
            console.error('학습량 그래프 로드 실패:', error);
            console.error('오류 상세:', error.stack);
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

                // 한국 시간대로 날짜 생성 (YYYY-MM-DD 형식)
                const dateYear = date.getFullYear();
                const dateMonth = String(date.getMonth() + 1).padStart(2, '0');
                const dateDay = String(date.getDate()).padStart(2, '0');
                const dateString = `${dateYear}-${dateMonth}-${dateDay}`;
                
                html += `
                    <div class="${cellClass}" data-date="${dateString}">
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
        document.getElementById('dashboard-current-level').textContent = `Level ${data.currentLevel}`;
        document.getElementById('dashboard-level-progress').textContent = `${data.levelProgress}%`;
        document.getElementById('dashboard-level-description').textContent =
            `다음 레벨까지 ${data.wordsToNextLevel}단어, ${data.sentencesToNextLevel}문장`;
    }

    // 연속 학습일 정보 UI 업데이트
    updateStreakInfoUI(data) {
        // 연속 학습일 수 업데이트
        this.updateElement('dashboard-streak-days', data.currentStreak);
        this.updateElement('dashboard-streak-duration', `${data.currentStreak} 일`);

        // 연속 학습일 메시지 표시
        const streakCard = document.querySelector('.stat-card:nth-child(4)');
        if (streakCard) {
            let messageElement = streakCard.querySelector('.streak-message');
            if (!messageElement) {
                messageElement = document.createElement('div');
                messageElement.className = 'streak-message';
                messageElement.style.fontSize = '12px';
                messageElement.style.color = '#666';
                messageElement.style.marginTop = '5px';
                streakCard.querySelector('.stat-info').appendChild(messageElement);
            }
            messageElement.textContent = data.streakMessage;
        }

        // 연속 학습일 보너스 표시
        if (data.streakBonus > 0) {
            let bonusElement = document.getElementById('dashboard-streak-bonus');
            if (!bonusElement) {
                bonusElement = document.createElement('div');
                bonusElement.id = 'dashboard-streak-bonus';
                bonusElement.className = 'streak-bonus';
                bonusElement.style.fontSize = '11px';
                bonusElement.style.color = '#eab308';
                bonusElement.style.fontWeight = 'bold';
                bonusElement.style.marginTop = '3px';
                
                const streakCard = document.querySelector('.stat-card:nth-child(4)');
                if (streakCard) {
                    streakCard.querySelector('.stat-info').appendChild(bonusElement);
                }
            }
            bonusElement.textContent = `+${data.streakBonus} 코인 보너스`;
            bonusElement.style.display = 'block';
        }

        // 다음 목표 메시지 표시
        if (data.daysToNextGoal > 0) {
            let goalElement = document.getElementById('dashboard-next-goal');
            if (!goalElement) {
                goalElement = document.createElement('div');
                goalElement.id = 'dashboard-next-goal';
                goalElement.className = 'next-goal';
                goalElement.style.fontSize = '10px';
                goalElement.style.color = '#888';
                goalElement.style.marginTop = '2px';
                
                const streakCard = document.querySelector('.stat-card:nth-child(4)');
                if (streakCard) {
                    streakCard.querySelector('.stat-info').appendChild(goalElement);
                }
            }
            goalElement.textContent = data.nextGoalMessage;
            goalElement.style.display = 'block';
        }
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
        this.loadStreakInfo();
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

    // Chart.js 동적 로드 메서드
    async loadChartJS() {
        return new Promise((resolve, reject) => {
            if (typeof Chart !== 'undefined') {
                console.log('Chart.js 이미 로드됨');
                resolve();
                return;
            }

            console.log('Chart.js 동적 로드 시작...');
            const script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.js';
            script.onload = () => {
                console.log('Chart.js 동적 로드 완료');
                // Chart 객체가 실제로 사용 가능한지 확인
                if (typeof Chart !== 'undefined') {
                    console.log('Chart 객체 사용 가능 확인됨');
                    resolve();
                } else {
                    console.error('Chart 객체가 여전히 정의되지 않음');
                    reject(new Error('Chart 객체 초기화 실패'));
                }
            };
            script.onerror = () => {
                console.error('Chart.js 동적 로드 실패');
                reject(new Error('Chart.js 로드 실패'));
            };
            document.head.appendChild(script);
        });
    }
}

// 전역 DashboardManager 인스턴스
let dashboardManager;

// DOM 로드 완료 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('Dashboard DOM 로드 완료');
    
    // 대시보드 컨테이너가 존재하면 초기화
    const dashboardContainer = document.querySelector('.dashboard-container');
    if (dashboardContainer) {
        console.log('대시보드 컨테이너 발견 - DashboardManager 초기화');
        dashboardManager = new DashboardManager();
    } else {
        console.log('대시보드 컨테이너를 찾을 수 없음');
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

// 수동 초기화 함수 (디버깅용)
window.initDashboard = function() {
    console.log('수동으로 DashboardManager 초기화');
    dashboardManager = new DashboardManager();
};

async function loadRankings() {
    try {
        const response = await fetch('/learning/api/dashboard/rankings');
        if (!response.ok) throw new Error('랭킹 데이터 로드 실패');
        const rankings = await response.json();
        updateRankingUI(rankings);
    } catch (e) {
        console.error('랭킹 로드 실패:', e);
    }
}

function updateRankingUI(rankings) {
    const rankingList = document.querySelector('.ranking-list');
    if (!rankingList) return;
    rankingList.innerHTML = '';
    rankings.forEach(r => {
        rankingList.innerHTML += `
            <div class="rank-item rank-${r.rank}">
                <div class="rank-position">${r.rank}</div>
                <div class="rank-info">
                    <div class="rank-name">${r.name}</div>
                    <div class="rank-details">단어 ${r.wordsLearned}개 · 문장 ${r.sentencesLearned}개</div>
                </div>
                <div class="rank-badge">${r.badge}</div>
                <div class="rank-score">⭐ ${r.rank}위</div>
            </div>
        `;
    });
}

async function loadLevelProgress() {
    try {
        const res = await fetch('/api/level/progress');
        if (!res.ok) {
            console.error('레벨 진행도 API 호출 실패:', res.status);
            return;
        }
        const data = await res.json();
        console.log('레벨 진행도 데이터:', data);

        // 레벨 정보 업데이트
        const levelElement = document.getElementById('dashboard-current-level');
        if (levelElement) {
            levelElement.textContent = `Level ${data.currentLevel || 1}`;
        }

        // 진행률 퍼센트 업데이트
        const progressElement = document.getElementById('dashboard-level-progress');
        if (progressElement) {
            const progressPercent = Math.round(data.levelProgress || 0);
            progressElement.textContent = `${progressPercent}%`;
        }

        // 설명 업데이트
        const descriptionElement = document.getElementById('dashboard-level-description');
        if (descriptionElement) {
            const wordsToNext = data.wordsToNextLevel || 0;
            const sentencesToNext = data.sentencesToNextLevel || 0;
            descriptionElement.textContent = `다음 레벨까지 ${wordsToNext}단어, ${sentencesToNext}문장`;
        }

        // 원형 진행률 차트 업데이트
        const circle = document.querySelector('.progress-circle');
        if (circle) {
            const progressPercent = data.levelProgress || 0;
            const degrees = (progressPercent / 100) * 360;
            circle.style.background = `conic-gradient(#1976d2 ${degrees}deg, #e0e0e0 ${degrees}deg)`;
            console.log('원형 차트 업데이트:', progressPercent + '%', degrees + 'deg');
        } else {
            console.warn('progress-circle 요소를 찾을 수 없습니다.');
        }

        // 100% 달성 시 레벨업
        if (data.levelProgress >= 100) {
            console.log('레벨업 조건 달성!');
            await levelUp();
            // 레벨업 후 다시 진행도 불러오기
            await loadLevelProgress();
        }
    } catch (error) {
        console.error('레벨 진행도 로드 실패:', error);
    }
}

async function levelUp() {
    const res = await fetch('/api/level/levelup', { method: 'POST' });
    if (res.ok) {
        alert('레벨업! 축하합니다!');
    } else {
        alert('레벨업 처리 중 오류가 발생했습니다.');
    }
}

window.addEventListener('DOMContentLoaded', loadLevelProgress);
