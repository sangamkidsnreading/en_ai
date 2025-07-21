// 중복 선언 방지
if (typeof window.StudentManagementManager !== 'undefined') {
    console.log('🔄 기존 StudentManagementManager 제거');
    delete window.StudentManagementManager;
}

// 학생 관리 매니저 클래스
class StudentManagementManager {
    constructor() {
        this.baseUrl = '/api/teacher';
        this.currentStudentId = null;
        this.currentMonth = new Date();
        this.progressChart = null;
        this.init();
    }

    init() {
        console.log('학생 관리 매니저 초기화 중...');
        this.loadStudents();
        this.setupEventListeners();
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 학생 검색
        const searchBtn = document.getElementById('student-search-btn');
        const searchInput = document.getElementById('student-search-input');
        
        if (searchBtn) {
            searchBtn.addEventListener('click', () => {
                this.searchStudents(searchInput?.value || '');
            });
        }
        
        if (searchInput) {
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.searchStudents(searchInput.value);
                }
            });
        }

        // 상세보기 버튼들 (동적으로 생성된 버튼들)
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('view-detail-btn')) {
                const studentId = e.target.dataset.studentId;
                if (studentId) {
                    this.showStudentDetail(studentId);
                }
            }
        });

        // 탭 전환
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('tab-btn')) {
                this.switchTab(e.target.dataset.tab);
            }
        });

        // 달력 컨트롤
        const prevMonthBtn = document.getElementById('prev-month-btn');
        const nextMonthBtn = document.getElementById('next-month-btn');
        
        if (prevMonthBtn) {
            prevMonthBtn.addEventListener('click', () => {
                this.currentMonth.setMonth(this.currentMonth.getMonth() - 1);
                this.renderCalendar();
            });
        }
        
        if (nextMonthBtn) {
            nextMonthBtn.addEventListener('click', () => {
                this.currentMonth.setMonth(this.currentMonth.getMonth() + 1);
                this.renderCalendar();
            });
        }

        // 그래프 컨트롤
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('graph-btn')) {
                this.switchGraph(e.target.dataset.graph);
            }
        });

        // Day/Level 필터 변경 이벤트
        document.addEventListener('change', async (e) => {
            if (e.target.id === 'day-filter' || e.target.id === 'level-filter') {
                // 현재 Day/Level 진행도 그래프가 활성화된 경우에만 차트 업데이트
                const activeGraphBtn = document.querySelector('.graph-btn.active');
                if (activeGraphBtn && activeGraphBtn.dataset.graph === 'day-level') {
                    await this.loadDayLevelProgressGraph();
                }
            }
        });

        // 상세 정보 닫기
        const closeDetailBtn = document.getElementById('close-detail-btn');
        if (closeDetailBtn) {
            closeDetailBtn.addEventListener('click', async () => {
                await this.hideStudentDetail();
            });
        }

        // 피드백 제출
        const feedbackForm = document.getElementById('feedback-form');
        if (feedbackForm) {
            feedbackForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitFeedback();
            });
        }
    }

    // 학생 목록 로드
    async loadStudents(searchQuery = '') {
        try {
            console.log('학생 목록 로드 중...');
            const url = searchQuery ? 
                `${this.baseUrl}/students?searchQuery=${encodeURIComponent(searchQuery)}` : 
                `${this.baseUrl}/students`;
            
            const response = await fetch(url);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const students = await response.json();
            this.renderStudentList(students);
            console.log('학생 목록 로드 완료:', students);
        } catch (error) {
            console.error('학생 목록 로드 실패:', error);
            this.showError('학생 목록을 불러올 수 없습니다.');
        }
    }

    // 학생 검색
    searchStudents(query) {
        console.log('학생 검색:', query);
        this.loadStudents(query);
    }

    // 학생 목록 렌더링
    renderStudentList(students) {
        const tbody = document.querySelector('.student-list-table tbody');
        if (!tbody) return;

        if (students.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">학생이 없습니다.</td></tr>';
            return;
        }

        tbody.innerHTML = students.map(student => {
            const progress = student.progress || {};
            const totalProgress = (progress.totalWords || 0) + (progress.totalSentences || 0);
            
            return `
            <tr>
                <td>${student.name || '이름 없음'}</td>
                <td>${student.email || '이메일 없음'}</td>
                <td>${student.grade || '학년 정보 없음'}</td>
                    <td>${student.groupName || '그룹 없음'}</td>
                    <td>
                        <div class="progress-bar">
                            <div class="progress-fill" style="width: ${Math.min(totalProgress / 10 * 100, 100)}%"></div>
                        </div>
                        <span class="progress-text">${totalProgress}개</span>
                    </td>
                <td>
                    <button class="view-detail-btn" data-student-id="${student.id}">상세</button>
                </td>
            </tr>
            `;
        }).join('');
    }

    // 학생 상세 정보 표시
    async showStudentDetail(studentId) {
        try {
            console.log('학생 상세 정보 로드 중:', studentId);
            this.currentStudentId = studentId;

            // 로딩 표시 시작
            this.showLoading();

            // 학생 기본 정보 로드
            console.log('학생 기본 정보 로드 중...');
            const studentResponse = await fetch(`${this.baseUrl}/students/${studentId}`);
            if (!studentResponse.ok) throw new Error('학생 정보 로드 실패');
            const student = await studentResponse.json();
            console.log('학생 기본 정보:', student);

            // 학생 통계 로드
            console.log('학생 통계 로드 중...');
            const statsResponse = await fetch(`${this.baseUrl}/students/${studentId}/stats`);
            if (!statsResponse.ok) throw new Error('학생 통계 로드 실패');
            const stats = await statsResponse.json();
            console.log('학생 통계:', stats);

            // 로딩 표시 종료
            this.hideLoading();

            // UI 업데이트
            console.log('UI 업데이트 시작...');
            this.updateStudentDetailUI(student, stats);
            
            // 상세 섹션 표시
            const detailSection = document.querySelector('.student-detail-section');
            if (detailSection) {
                detailSection.style.display = 'block';
                console.log('상세 섹션 표시됨');
            } else {
                console.error('상세 섹션을 찾을 수 없습니다!');
            }

            // 달력 렌더링
            console.log('달력 렌더링 시작...');
            this.renderCalendar();

            console.log('학생 상세 정보 로드 완료');
        } catch (error) {
            // 로딩 표시 종료
            this.hideLoading();
            console.error('학생 상세 정보 로드 실패:', error);
            this.showError('학생 정보를 불러올 수 없습니다.');
        }
    }

    // 학생 상세 정보 UI 업데이트
    updateStudentDetailUI(student, stats) {
        console.log('updateStudentDetailUI 호출됨');
        
        // 기본 정보 업데이트
        const nameEl = document.getElementById('detail-student-name');
        const emailEl = document.getElementById('detail-student-email');
        const gradeEl = document.getElementById('detail-student-grade');
        const groupEl = document.getElementById('detail-student-group');

        console.log('기본 정보 요소들:', { nameEl, emailEl, gradeEl, groupEl });

        if (nameEl) {
            nameEl.textContent = student.name || '이름 없음';
            console.log('이름 업데이트:', student.name);
        }
        if (emailEl) {
            emailEl.textContent = student.email || '이메일 없음';
            console.log('이메일 업데이트:', student.email);
        }
        if (gradeEl) {
            gradeEl.textContent = student.grade || '학년 정보 없음';
            console.log('학년 업데이트:', student.grade);
        }
        if (groupEl) {
            groupEl.textContent = student.groupName || '그룹 없음';
            console.log('그룹 업데이트:', student.groupName);
        }

        // 통계 업데이트
        console.log('통계 업데이트 시작...');
        this.updateStatsUI(stats);
        console.log('UI 업데이트 완료');
    }

    // 통계 UI 업데이트
    updateStatsUI(stats) {
        console.log('updateStatsUI 호출됨, stats:', stats);
        
        const totalWordsEl = document.getElementById('total-words');
        const totalSentencesEl = document.getElementById('total-sentences');
        const todayWordsEl = document.getElementById('today-words');
        const todaySentencesEl = document.getElementById('today-sentences');
        const weekWordsEl = document.getElementById('week-words');
        const weekSentencesEl = document.getElementById('week-sentences');

        console.log('통계 요소들:', { 
            totalWordsEl, totalSentencesEl, todayWordsEl, 
            todaySentencesEl, weekWordsEl, weekSentencesEl 
        });

        if (totalWordsEl) {
            totalWordsEl.textContent = stats.totalWordsLearned || 0;
            console.log('전체 단어 수 업데이트:', stats.totalWordsLearned);
        }
        if (totalSentencesEl) {
            totalSentencesEl.textContent = stats.totalSentencesLearned || 0;
            console.log('전체 문장 수 업데이트:', stats.totalSentencesLearned);
        }
        if (todayWordsEl) {
            todayWordsEl.textContent = stats.todayWordsLearned || 0;
            console.log('오늘 단어 수 업데이트:', stats.todayWordsLearned);
        }
        if (todaySentencesEl) {
            todaySentencesEl.textContent = stats.todaySentencesLearned || 0;
            console.log('오늘 문장 수 업데이트:', stats.todaySentencesLearned);
        }
        if (weekWordsEl) {
            weekWordsEl.textContent = stats.weekWordsLearned || 0;
            console.log('이번 주 단어 수 업데이트:', stats.weekWordsLearned);
        }
        if (weekSentencesEl) {
            weekSentencesEl.textContent = stats.weekSentencesLearned || 0;
            console.log('이번 주 문장 수 업데이트:', stats.weekSentencesLearned);
        }
    }

    // 달력 렌더링
    async renderCalendar() {
        if (!this.currentStudentId) return;

        try {
            const year = this.currentMonth.getFullYear();
            const month = this.currentMonth.getMonth() + 1;
            
            // 월 표시 업데이트
            const currentMonthEl = document.getElementById('current-month');
            if (currentMonthEl) {
                currentMonthEl.textContent = `${year}년 ${month}월`;
            }

            // 학습 데이터 로드
            const startDate = `${year}-${month.toString().padStart(2, '0')}-01`;
            const endDate = new Date(year, month, 0).toISOString().split('T')[0];
            
            const response = await fetch(`${this.baseUrl}/students/${this.currentStudentId}/learning-data?startDate=${startDate}&endDate=${endDate}`);
            if (!response.ok) throw new Error('학습 데이터 로드 실패');
            
            const learningData = await response.json();
            
            // 달력 그리드 생성
            this.createCalendarGrid(year, month, learningData);
            
        } catch (error) {
            console.error('달력 렌더링 실패:', error);
        }
    }

    // 달력 그리드 생성
    createCalendarGrid(year, month, learningData) {
        const grid = document.getElementById('calendar-grid');
        if (!grid) return;

        const firstDay = new Date(year, month - 1, 1);
        const lastDay = new Date(year, month, 0);
        const startDate = new Date(firstDay);
        startDate.setDate(startDate.getDate() - firstDay.getDay());

        let html = `
            <div class="calendar-header">
                <div>일</div><div>월</div><div>화</div><div>수</div><div>목</div><div>금</div><div>토</div>
            </div>
        `;

        // 6주치 달력 생성
        for (let week = 0; week < 6; week++) {
            html += '<div class="calendar-week">';
            for (let day = 0; day < 7; day++) {
                const currentDate = new Date(startDate);
                currentDate.setDate(startDate.getDate() + week * 7 + day);
                
                const dateStr = currentDate.toISOString().split('T')[0];
                const dayData = learningData.find(d => d.date === dateStr);
                const totalLearned = dayData ? (dayData.wordsLearned || 0) + (dayData.sentencesLearned || 0) : 0;
                
                let activityClass = 'none';
                if (totalLearned >= 5) activityClass = 'high';
                else if (totalLearned >= 2) activityClass = 'medium';
                else if (totalLearned >= 1) activityClass = 'low';
                
                const isCurrentMonth = currentDate.getMonth() === month - 1;
                const isToday = currentDate.toDateString() === new Date().toDateString();
                
                html += `
                    <div class="calendar-day ${activityClass} ${isCurrentMonth ? 'current-month' : 'other-month'} ${isToday ? 'today' : ''}">
                        <span class="day-number">${currentDate.getDate()}</span>
                        ${totalLearned > 0 ? `<span class="activity-count">${totalLearned}</span>` : ''}
                    </div>
                `;
            }
            html += '</div>';
        }

        grid.innerHTML = html;
    }

    // 탭 전환
    switchTab(tabName) {
        // 탭 버튼 활성화
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

        // 탭 콘텐츠 전환
        document.querySelectorAll('.tab-content').forEach(content => {
            content.classList.remove('active');
        });
        document.getElementById(`${tabName}-tab`).classList.add('active');

        // 그래프 탭인 경우 기본 그래프 로드
        if (tabName === 'graph') {
            // 기본적으로 Day/Level 진행도 로드
            this.switchGraph('day-level');
        }
    }





    // 진행도 차트 렌더링
    async renderProgressChart(graphData, title = '학습 진행도') {
        const ctx = document.getElementById('progress-chart');
        if (!ctx) return;

        // 기존 차트 제거 확인
        await this.destroyChart();

        try {
            this.progressChart = new Chart(ctx, {
                type: 'line',
                data: {
                    labels: graphData.labels || [],
                    datasets: [{
                        label: '단어 학습',
                        data: graphData.wordsData || [],
                        borderColor: 'rgb(54, 162, 235)',
                        backgroundColor: 'rgba(54, 162, 235, 0.1)',
                        tension: 0.1
                    }, {
                        label: '문장 학습',
                        data: graphData.sentencesData || [],
                        borderColor: 'rgb(255, 99, 132)',
                        backgroundColor: 'rgba(255, 99, 132, 0.1)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: {
                            display: true,
                            text: title
                        },
                        legend: {
                            display: true,
                            position: 'top'
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: '학습 개수'
                            }
                        },
                        x: {
                            title: {
                                display: true,
                                text: '날짜'
                            }
                        }
                    }
                }
            });
        } catch (error) {
            // 차트 생성 오류 무시 (기능은 정상 작동)
            console.log('차트 렌더링 중 오류 (무시됨):', error.message);
        }
    }

    // 그래프 전환
    async switchGraph(graphType) {
        console.log('switchGraph 호출됨:', graphType);
        
        // 모든 그래프 버튼 비활성화
        document.querySelectorAll('.graph-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        
        // 해당 그래프 버튼 활성화 (요소가 존재하는 경우에만)
        const targetButton = document.querySelector(`[data-graph="${graphType}"]`);
        if (targetButton) {
            targetButton.classList.add('active');
        } else {
            console.warn(`그래프 버튼을 찾을 수 없습니다: ${graphType}`);
        }

        // Day/Level 필터 표시/숨김
        const dayLevelFilters = document.querySelector('.day-level-filters');
        if (dayLevelFilters) {
            dayLevelFilters.style.display = graphType === 'day-level' ? 'block' : 'none';
        }

        // 기존 차트 완전히 제거
        await this.destroyChart();

        // 그래프 타입에 따라 차트 업데이트
        if (graphType === 'day-level') {
            this.loadDayLevelProgressGraph();
        } else if (graphType === 'weekly') {
            this.loadWeeklyProgressGraph();
        } else {
            this.loadDailyProgressGraph();
        }
    }

    // 일별 진행도 그래프 로드
    async loadDailyProgressGraph() {
        if (!this.currentStudentId) return;

        try {
            // 기존 차트 제거
            await this.destroyChart();
            
            const response = await fetch(`${this.baseUrl}/students/${this.currentStudentId}/progress-graph`);
            if (!response.ok) throw new Error('일별 그래프 데이터 로드 실패');
            
            const graphData = await response.json();
            await this.renderProgressChart(graphData, '일별 학습 진행도');
            
        } catch (error) {
            console.error('일별 그래프 로드 실패:', error);
            this.showError('일별 그래프 데이터를 불러올 수 없습니다.');
        }
    }

    // 주별 진행도 그래프 로드
    async loadWeeklyProgressGraph() {
        if (!this.currentStudentId) return;

        try {
            // 기존 차트 제거
            await this.destroyChart();
            
            const response = await fetch(`${this.baseUrl}/students/${this.currentStudentId}/weekly-progress`);
            if (!response.ok) throw new Error('주별 그래프 데이터 로드 실패');
            
            const graphData = await response.json();
            await this.renderProgressChart(graphData, '주별 학습 진행도');
            
        } catch (error) {
            console.error('주별 그래프 로드 실패:', error);
            this.showError('주별 그래프 데이터를 불러올 수 없습니다.');
        }
    }

    // 차트 제거 메서드 - Chart.js 레지스트리에서 완전 제거
    destroyChart() {
        console.log('destroyChart 시작');
        
        // 기존 차트 인스턴스 제거
        if (this.progressChart) {
            console.log('기존 차트 인스턴스 제거 중...');
            try {
                this.progressChart.destroy();
                console.log('차트 인스턴스 제거 완료');
            } catch (error) {
                console.log('차트 인스턴스 제거 중 오류 (무시됨):', error);
            }
            this.progressChart = null;
        } else {
            console.log('제거할 차트 인스턴스가 없습니다.');
        }
        
        // Chart.js 레지스트리에서 해당 Canvas의 모든 차트 제거
        const canvas = document.getElementById('progress-chart');
        if (canvas && typeof Chart !== 'undefined') {
            console.log('Chart.js 레지스트리에서 차트 제거 중...');
            try {
                // Chart.js 레지스트리에서 해당 Canvas의 모든 차트 찾기
                const existingCharts = Chart.instances;
                if (existingCharts) {
                    Object.keys(existingCharts).forEach(chartId => {
                        const chart = existingCharts[chartId];
                        if (chart.ctx.canvas === canvas) {
                            console.log(`차트 ID ${chartId} 제거 중...`);
                            chart.destroy();
                        }
                    });
                }
                console.log('Chart.js 레지스트리 정리 완료');
            } catch (error) {
                console.log('Chart.js 레지스트리 정리 중 오류 (무시됨):', error);
            }
        }
        
        console.log('차트 제거 완료');
        return Promise.resolve();
    }

    // Day/Level별 진행도 그래프 로드
    async loadDayLevelProgressGraph() {
        if (!this.currentStudentId) return;

        try {
            // 기존 차트 제거
            await this.destroyChart();
            
            const response = await fetch(`${this.baseUrl}/students/${this.currentStudentId}/day-level-progress`);
            if (!response.ok) throw new Error('Day/Level 진행도 데이터 로드 실패');
            
            const dayLevelData = await response.json();
            console.log('Day/Level 진행도 데이터:', dayLevelData);
            
            await this.renderDayLevelProgressChart(dayLevelData);
            
            // Day 필터 옵션 업데이트
            this.updateDayFilterOptions(dayLevelData);
            
        } catch (error) {
            console.error('Day/Level 진행도 그래프 로드 실패:', error);
            this.showError('Day/Level 진행도 데이터를 불러올 수 없습니다.');
        }
    }

    // Day/Level 필터 옵션 업데이트
    updateDayFilterOptions(dayLevelData) {
        console.log('필터 옵션 업데이트 시작:', dayLevelData);
        
        const dayFilter = document.getElementById('day-filter');
        const levelFilter = document.getElementById('level-filter');
        
        if (!dayFilter || !levelFilter) {
            console.error('필터 요소를 찾을 수 없습니다.');
            return;
        }

        // 현재 선택된 값 저장
        const currentDayValue = dayFilter.value;
        const currentLevelValue = levelFilter.value;

        // 기존 옵션 제거 (전체 제외)
        const dayOptions = dayFilter.querySelectorAll('option:not([value="all"])');
        const levelOptions = levelFilter.querySelectorAll('option:not([value="all"])');
        
        dayOptions.forEach(option => option.remove());
        levelOptions.forEach(option => option.remove());

        // 고유한 Day 값들 추출
        const uniqueDays = [...new Set(dayLevelData.map(item => item.day))].sort((a, b) => a - b);
        console.log('고유한 Day 값들:', uniqueDays);
        
        // 고유한 Level 값들 추출
        const uniqueLevels = [...new Set(dayLevelData.map(item => item.level))].sort((a, b) => a - b);
        console.log('고유한 Level 값들:', uniqueLevels);
        
        // Day 옵션 추가
        uniqueDays.forEach(day => {
            const option = document.createElement('option');
            option.value = day;
            option.textContent = `Day ${day}`;
            dayFilter.appendChild(option);
        });
        
        // Level 옵션 추가
        uniqueLevels.forEach(level => {
            const option = document.createElement('option');
            option.value = level;
            option.textContent = `Level ${level}`;
            levelFilter.appendChild(option);
        });
        
        // 이전 선택 값 복원 (유효한 경우에만)
        if (currentDayValue && currentDayValue !== 'all' && uniqueDays.includes(parseInt(currentDayValue))) {
            dayFilter.value = currentDayValue;
        }
        if (currentLevelValue && currentLevelValue !== 'all' && uniqueLevels.includes(parseInt(currentLevelValue))) {
            levelFilter.value = currentLevelValue;
        }
        
        console.log('필터 옵션 업데이트 완료');
    }

    // Day/Level 진행도 차트 렌더링
    async renderDayLevelProgressChart(dayLevelData) {
        console.log('renderDayLevelProgressChart 시작');
        
        // Canvas 요소 확인 및 크기 설정
        const ctx = document.getElementById('progress-chart');
        if (!ctx) {
            console.error('progress-chart canvas를 찾을 수 없습니다.');
            return;
        }
        
        // Canvas 크기 명시적 설정
        const container = ctx.parentElement;
        const containerWidth = Math.max(container.offsetWidth - 40, 600); // 최소 600px
        const containerHeight = 400;
        
        console.log('컨테이너 크기:', container.offsetWidth, 'x', container.offsetHeight);
        console.log('설정할 Canvas 크기:', containerWidth, 'x', containerHeight);
        
        // Canvas 크기 설정
        ctx.style.width = containerWidth + 'px';
        ctx.style.height = containerHeight + 'px';
        ctx.width = containerWidth;
        ctx.height = containerHeight;
        
        console.log('Canvas 크기 설정 완료:', ctx.width, 'x', ctx.height);
        
        console.log('Canvas 찾음:', ctx);
        console.log('Canvas 크기:', ctx.width, 'x', ctx.height);

        // 기존 차트 제거 확인
        await this.destroyChart();
        console.log('기존 차트 제거 완료');

        // 필터 적용
        const selectedDay = document.getElementById('day-filter')?.value;
        const selectedLevel = document.getElementById('level-filter')?.value;
        
        let filteredData = dayLevelData;
        
        if (selectedDay && selectedDay !== 'all') {
            filteredData = filteredData.filter(item => item.day == selectedDay);
        }
        
        if (selectedLevel && selectedLevel !== 'all') {
            filteredData = filteredData.filter(item => item.level == selectedLevel);
        }

        console.log('필터링된 Day/Level 데이터:', filteredData);

        // 데이터가 없는 경우 처리
        if (!filteredData || filteredData.length === 0) {
            console.log('Day/Level 진행도 데이터가 없습니다.');
            this.showEmptyChart('Day/Level별 학습 진행도', '학습 데이터가 없습니다.');
            return;
        }

        // 차트 데이터 준비
        const labels = filteredData.map(item => `Day ${item.day} - Level ${item.level}`);
        const wordsData = filteredData.map(item => {
            const value = item.wordsLearned;
            return typeof value === 'number' ? value : parseInt(value) || 0;
        });
        const sentencesData = filteredData.map(item => {
            const value = item.sentencesLearned;
            return typeof value === 'number' ? value : parseInt(value) || 0;
        });
        const totalData = filteredData.map((item, index) => wordsData[index] + sentencesData[index]);

        console.log('원본 필터링된 데이터:', filteredData);
        console.log('차트 데이터:', { 
            labels: labels, 
            wordsData: wordsData, 
            sentencesData: sentencesData, 
            totalData: totalData 
        });
        console.log('데이터 매핑 상세:');
        filteredData.forEach((item, index) => {
            console.log(`인덱스 ${index}: Day ${item.day}, Level ${item.level}, 단어 ${item.wordsLearned} (${typeof item.wordsLearned}), 문장 ${item.sentencesLearned} (${typeof item.sentencesLearned})`);
        });

        // Chart.js가 로드되었는지 확인
        if (typeof Chart === 'undefined') {
            console.error('Chart.js가 로드되지 않았습니다.');
            this.showError('Chart.js 라이브러리를 불러올 수 없습니다.');
            return;
        }
        console.log('Chart.js 확인됨');

        try {
            console.log('차트 생성 시작...');
            this.progressChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: '단어 학습',
                            data: wordsData,
                            backgroundColor: 'rgba(54, 162, 235, 0.8)',
                            borderColor: 'rgba(54, 162, 235, 1)',
                            borderWidth: 1
                        },
                        {
                            label: '문장 학습',
                            data: sentencesData,
                            backgroundColor: 'rgba(255, 99, 132, 0.8)',
                            borderColor: 'rgba(255, 99, 132, 1)',
                            borderWidth: 1
                        },
                        {
                            label: '총 학습량',
                            data: totalData,
                            backgroundColor: 'rgba(75, 192, 192, 0.8)',
                            borderColor: 'rgba(75, 192, 192, 1)',
                            borderWidth: 1
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    layout: {
                        padding: {
                            top: 20,
                            bottom: 20
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: '학습 개수'
                            }
                        },
                        x: {
                            title: {
                                display: true,
                                text: 'Day - Level'
                            }
                        }
                    },
                    plugins: {
                        title: {
                            display: true,
                            text: 'Day/Level별 학습 진행도'
                        },
                        legend: {
                            display: true,
                            position: 'top'
                        }
                    }
                }
            });
            console.log('차트 생성 완료:', this.progressChart);
            
            // 차트 생성 후 간단한 업데이트
            setTimeout(() => {
                if (this.progressChart) {
                    this.progressChart.update();
                    console.log('차트 업데이트 완료');
                    console.log('최종 차트 크기:', this.progressChart.width, 'x', this.progressChart.height);
                    console.log('최종 Canvas 크기:', ctx.width, 'x', ctx.height);
                }
            }, 100);
            
        } catch (error) {
            console.error('차트 생성 중 오류 발생:', error);
            this.showError('차트를 생성할 수 없습니다: ' + error.message);
        }
    }

    // 빈 차트 표시 (데이터가 없는 경우)
    showEmptyChart(title, message) {
        const ctx = document.getElementById('progress-chart');
        if (!ctx) return;

        try {
            this.progressChart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['데이터 없음'],
                    datasets: [{
                        label: '학습량',
                        data: [0],
                        backgroundColor: 'rgba(200, 200, 200, 0.5)',
                        borderColor: 'rgba(200, 200, 200, 1)',
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    plugins: {
                        title: {
                            display: true,
                            text: title
                        },
                        legend: {
                            display: false
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            title: {
                                display: true,
                                text: '학습 개수'
                            }
                        }
                    }
                }
            });
        } catch (error) {
            console.log('빈 차트 렌더링 중 오류 (무시됨):', error.message);
        }
    }

    // 피드백 제출
    async submitFeedback() {
        if (!this.currentStudentId) {
            this.showError('학생을 선택해주세요.');
            return;
        }

        const contentEl = document.getElementById('feedback-content');
        const content = contentEl?.value?.trim();
        
        if (!content) {
            this.showError('피드백 내용을 입력해주세요.');
            return;
        }

        try {
            console.log('피드백 제출 중...');
            const response = await fetch(`${this.baseUrl}/students/${this.currentStudentId}/feedback`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ content })
            });

            if (!response.ok) throw new Error('피드백 제출 실패');
            
            const result = await response.json();
            console.log('피드백 제출 완료:', result);
            
            // 피드백 입력창 초기화
            if (contentEl) contentEl.value = '';
            
            // 피드백 목록 새로고침
            this.refreshFeedbackList();
            
            this.showSuccess('피드백이 저장되었습니다.');
        } catch (error) {
            console.error('피드백 제출 실패:', error);
            this.showError('피드백 저장에 실패했습니다.');
        }
    }

    // 피드백 목록 새로고침
    async refreshFeedbackList() {
        if (!this.currentStudentId) return;

        try {
            const response = await fetch(`${this.baseUrl}/students/${this.currentStudentId}/feedback`);
            if (!response.ok) throw new Error('피드백 목록 로드 실패');
            
            const feedbacks = await response.json();
            this.renderFeedbackHistory(feedbacks);
        } catch (error) {
            console.error('피드백 목록 새로고침 실패:', error);
        }
    }

    // 피드백 히스토리 렌더링
    renderFeedbackHistory(feedbacks) {
        const feedbackHistory = document.getElementById('feedback-history');
        if (!feedbackHistory) return;

        const ul = feedbackHistory.querySelector('ul');
        if (!ul) return;

        if (feedbacks.length === 0) {
            ul.innerHTML = '<li>작성된 피드백이 없습니다.</li>';
            return;
        }

        ul.innerHTML = feedbacks.map(feedback => `
            <li>
                <span>${feedback.content || '내용 없음'}</span>
                <small>${feedback.createdAt ? new Date(feedback.createdAt).toLocaleString() : ''}</small>
                <button type="button" class="edit-feedback-btn" data-feedback-id="${feedback.id}">수정</button>
                <button type="button" class="delete-feedback-btn" data-feedback-id="${feedback.id}">삭제</button>
            </li>
        `).join('');
    }

    // 학생 상세 정보 숨기기
    async hideStudentDetail() {
        const detailSection = document.querySelector('.student-detail-section');
        if (detailSection) {
            detailSection.style.display = 'none';
        }
        this.currentStudentId = null;
        
        // 차트 정리
        await this.destroyChart();
    }

    // 로딩 표시
    showLoading() {
        // 기존 로딩 오버레이가 있으면 제거
        this.hideLoading();
        
        // 로딩 오버레이 생성
        const loadingOverlay = document.createElement('div');
        loadingOverlay.id = 'student-detail-loading';
        loadingOverlay.className = 'loading-overlay';
        loadingOverlay.innerHTML = `
            <div class="loading-spinner"></div>
            <div class="loading-text">학생 정보를 불러오는 중...</div>
        `;
        
        document.body.appendChild(loadingOverlay);
        
        // 상세 섹션 표시 (내용은 덮어쓰지 않음)
        const detailSection = document.querySelector('.student-detail-section');
        if (detailSection) {
            detailSection.style.display = 'block';
        }
    }

    // 로딩 표시 숨기기
    hideLoading() {
        // 로딩 오버레이 제거
        const loadingOverlay = document.getElementById('student-detail-loading');
        if (loadingOverlay) {
            loadingOverlay.remove();
        }
    }

    // 성공 메시지 표시
    showSuccess(message) {
        // 토스트 메시지 또는 알림 표시
        console.log('✅', message);
        alert(message);
    }

    // 에러 메시지 표시
    showError(message) {
        console.error('❌', message);
        alert('오류: ' + message);
    }
}

// 전역에서 사용할 수 있도록 설정
window.StudentManagementManager = StudentManagementManager;

// 초기화 함수
function initStudentManagementManager() {
    if (window.studentManagementManager) {
        window.studentManagementManager = null;
    }

    setTimeout(() => {
        try {
            window.studentManagementManager = new StudentManagementManager();
            console.log('✅ 학생 관리 매니저 초기화 완료');
        } catch (error) {
            console.error('❌ 학생 관리 매니저 초기화 실패:', error);
        }
    }, 100);
}

// DOMContentLoaded에서 안전하게 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initStudentManagementManager);
} else {
    initStudentManagementManager();
}