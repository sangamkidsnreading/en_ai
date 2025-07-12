// 학생 관리 매니저 클래스
class StudentManagementManager {
    constructor() {
        this.baseUrl = '/api/teacher';
        this.currentStudentId = null;
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

        // 피드백 폼 제출
        const feedbackForm = document.getElementById('feedback-form');
        if (feedbackForm) {
            feedbackForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.submitFeedback();
            });
        }

        // 닫기 버튼
        const closeBtn = document.getElementById('close-detail-btn');
        if (closeBtn) {
            closeBtn.addEventListener('click', () => {
                this.hideStudentDetail();
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
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">학생이 없습니다.</td></tr>';
            return;
        }

        tbody.innerHTML = students.map(student => `
            <tr>
                <td>${student.name || '이름 없음'}</td>
                <td>${student.email || '이메일 없음'}</td>
                <td>${student.grade || '학년 정보 없음'}</td>
                <td>
                    <button class="view-detail-btn" data-student-id="${student.id}">상세</button>
                </td>
            </tr>
        `).join('');
    }

    // 학생 상세 정보 표시
    async showStudentDetail(studentId) {
        try {
            console.log('학생 상세 정보 로드 중:', studentId);
            this.currentStudentId = studentId;

            // 로딩 표시 시작
            this.showLoading();

            // 학생 기본 정보 로드
            const studentResponse = await fetch(`${this.baseUrl}/students/${studentId}`);
            if (!studentResponse.ok) throw new Error('학생 정보 로드 실패');
            const student = await studentResponse.json();

            // 학생 통계 로드
            const statsResponse = await fetch(`${this.baseUrl}/students/${studentId}/stats`);
            if (!statsResponse.ok) throw new Error('학생 통계 로드 실패');
            const stats = await statsResponse.json();

            // 학생 학습 데이터 로드 (최근 30일)
            const learningResponse = await fetch(`${this.baseUrl}/students/${studentId}/learning-data`);
            if (!learningResponse.ok) throw new Error('학습 데이터 로드 실패');
            const learningData = await learningResponse.json();

            // 학생 피드백 로드
            const feedbackResponse = await fetch(`${this.baseUrl}/students/${studentId}/feedback`);
            if (!feedbackResponse.ok) throw new Error('피드백 로드 실패');
            const feedbacks = await feedbackResponse.json();

            // 로딩 표시 종료
            this.hideLoading();

            // UI 업데이트
            this.updateStudentDetailUI(student, stats, learningData, feedbacks);
            
            // 상세 섹션 표시
            const detailSection = document.querySelector('.student-detail-section');
            if (detailSection) {
                detailSection.style.display = 'block';
            }

            console.log('학생 상세 정보 로드 완료');
        } catch (error) {
            // 로딩 표시 종료
            this.hideLoading();
            console.error('학생 상세 정보 로드 실패:', error);
            this.showError('학생 정보를 불러올 수 없습니다.');
        }
    }

    // 학생 상세 정보 UI 업데이트
    updateStudentDetailUI(student, stats, learningData, feedbacks) {
        // 기본 정보 업데이트
        const nameEl = document.getElementById('detail-student-name');
        const emailEl = document.getElementById('detail-student-email');
        const gradeEl = document.getElementById('detail-student-grade');

        if (nameEl) nameEl.textContent = student.name || '이름 없음';
        if (emailEl) emailEl.textContent = student.email || '이메일 없음';
        if (gradeEl) gradeEl.textContent = student.grade || '학년 정보 없음';

        // 학습 데이터 테이블 업데이트
        this.renderLearningDataTable(learningData);

        // 피드백 히스토리 업데이트
        this.renderFeedbackHistory(feedbacks);
    }

    // 학습 데이터 테이블 렌더링
    renderLearningDataTable(learningData) {
        const tbody = document.getElementById('learning-data-body');
        if (!tbody) return;

        if (learningData.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">학습 데이터가 없습니다.</td></tr>';
            return;
        }

        tbody.innerHTML = learningData.map(day => `
            <tr>
                <td>${day.date}</td>
                <td>${day.wordsLearned || 0}개</td>
                <td>${day.sentencesLearned || 0}개</td>
                <td>${day.coinsEarned || 0}개</td>
            </tr>
        `).join('');
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

    // 학생 상세 정보 숨기기
    hideStudentDetail() {
        const detailSection = document.querySelector('.student-detail-section');
        if (detailSection) {
            detailSection.style.display = 'none';
        }
        this.currentStudentId = null;
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
        
        // 상세 섹션에 로딩 표시
        const detailSection = document.querySelector('.student-detail-section');
        if (detailSection) {
            detailSection.style.display = 'block';
            detailSection.innerHTML = `
                <div class="detail-loading">
                    <div class="loading-spinner"></div>
                    <div class="loading-text">학생 정보를 불러오는 중...</div>
                </div>
            `;
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