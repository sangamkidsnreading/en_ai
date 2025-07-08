
// 중복 선언 방지
if (typeof window.DarkModeManager !== 'undefined') {
    console.log('🔄 기존 DarkModeManager 제거');
    delete window.DarkModeManager;
}

// 다크모드 관리자
class DarkModeManager {
    constructor() {
        this.isDarkMode = false;
        this.init();
    }

    init() {
        console.log('🌙 다크모드 관리자 초기화');
        
        // 저장된 테마 설정 로드
        this.loadSavedTheme();
        
        // 다크모드 토글 버튼 이벤트 설정
        this.setupToggleEvent();
        
        // 시스템 테마 변경 감지
        this.setupSystemThemeDetection();
        
        console.log('✅ 다크모드 관리자 초기화 완료');
    }

    // 저장된 테마 설정 로드
    loadSavedTheme() {
        const savedTheme = localStorage.getItem('dark-mode');
        const systemPrefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        
        if (savedTheme === 'dark' || (savedTheme === null && systemPrefersDark)) {
            this.enableDarkMode();
        } else {
            this.disableDarkMode();
        }
    }

    // 토글 버튼 이벤트 설정
    setupToggleEvent() {
        const toggleButton = document.getElementById('dark-mode-toggle');
        
        if (toggleButton) {
            toggleButton.addEventListener('click', () => {
                this.toggleDarkMode();
            });
        }
    }

    // 시스템 테마 변경 감지
    setupSystemThemeDetection() {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
        mediaQuery.addListener((e) => {
            if (localStorage.getItem('dark-mode') === null) {
                if (e.matches) {
                    this.enableDarkMode();
                } else {
                    this.disableDarkMode();
                }
            }
        });
    }

    // 다크모드 토글
    toggleDarkMode() {
        if (this.isDarkMode) {
            this.disableDarkMode();
            localStorage.setItem('dark-mode', 'light');
        } else {
            this.enableDarkMode();
            localStorage.setItem('dark-mode', 'dark');
        }
        
        // 토글 애니메이션
        this.animateToggle();
    }

    // 다크모드 활성화
    enableDarkMode() {
        document.documentElement.classList.add('dark-mode');
        document.body.classList.add('dark-mode');
        this.isDarkMode = true;
        
        this.updateToggleButton();
        
        console.log('🌙 다크모드 활성화');
    }

    // 다크모드 비활성화
    disableDarkMode() {
        document.documentElement.classList.remove('dark-mode');
        document.body.classList.remove('dark-mode');
        this.isDarkMode = false;
        
        this.updateToggleButton();
        
        console.log('☀️ 라이트모드 활성화');
    }

    // 토글 버튼 업데이트
    updateToggleButton() {
        const themeIcon = document.getElementById('theme-icon');
        const themeText = document.getElementById('theme-text');
        
        if (themeIcon && themeText) {
            if (this.isDarkMode) {
                themeIcon.textContent = '☀️';
                themeText.textContent = '라이트모드';
            } else {
                themeIcon.textContent = '🌙';
                themeText.textContent = '다크모드';
            }
        }
    }

    // 토글 애니메이션
    animateToggle() {
        const toggleButton = document.getElementById('dark-mode-toggle');
        
        if (toggleButton) {
            toggleButton.style.transform = 'scale(0.95)';
            
            setTimeout(() => {
                toggleButton.style.transform = 'scale(1)';
            }, 150);
        }
    }
}

// 다크모드 CSS 스타일 추가
const darkModeStyles = `
    /* 다크모드 전체 스타일 */
    .dark-mode {
        --bg-primary: #121212;
        --bg-secondary: #1e1e1e;
        --bg-tertiary: #2d2d2d;
        --bg-quaternary: #404040;
        --text-primary: #ffffff;
        --text-secondary: #b3b3b3;
        --text-tertiary: #808080;
        --border-color: #333333;
        --border-light: #404040;
        --accent-color: #4CAF50;
        --accent-hover: #45a049;
        --accent-secondary: #2196F3;
        --accent-tertiary: #FF9800;
        --shadow: rgba(0, 0, 0, 0.4);
        --shadow-light: rgba(0, 0, 0, 0.2);
        --gradient-primary: linear-gradient(135deg, #1e1e1e 0%, #2d2d2d 100%);
        --gradient-secondary: linear-gradient(135deg, #2d2d2d 0%, #404040 100%);
    }

    /* 기본 라이트모드 변수 */
    :root {
        --bg-primary: #ffffff;
        --bg-secondary: #f8f9fa;
        --bg-tertiary: #e9ecef;
        --bg-quaternary: #dee2e6;
        --text-primary: #333333;
        --text-secondary: #666666;
        --text-tertiary: #999999;
        --border-color: #e9ecef;
        --border-light: #f8f9fa;
        --accent-color: #007bff;
        --accent-hover: #0056b3;
        --accent-secondary: #28a745;
        --accent-tertiary: #ffc107;
        --shadow: rgba(0, 0, 0, 0.1);
        --shadow-light: rgba(0, 0, 0, 0.05);
        --gradient-primary: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
        --gradient-secondary: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    }

    /* 다크모드 전체 배경 */
    .dark-mode body {
        background-color: var(--bg-primary);
        color: var(--text-primary);
        transition: background-color 0.3s ease, color 0.3s ease;
    }

    /* 사이드바 다크모드 */
    .dark-mode .sidebar {
        background-color: var(--bg-secondary);
        border-right-color: var(--border-color);
        box-shadow: 0 10px 15px -3px var(--shadow);
    }

    .dark-mode .sidebar-header {
        border-bottom-color: var(--border-color);
    }

    .dark-mode .logo-text {
        color: var(--text-primary);
    }

    .dark-mode .logo-subtitle {
        color: var(--text-secondary);
    }

    /* 테마 토글 버튼 다크모드 */
    .dark-mode .theme-toggle {
        background-color: var(--bg-tertiary);
        color: var(--text-primary);
    }

    .dark-mode .theme-toggle:hover {
        background-color: var(--bg-primary);
    }

    /* 필터 영역 다크모드 */
    .dark-mode .filter-label {
        color: var(--text-secondary);
    }

    .dark-mode .search-input,
    .dark-mode .filter-select {
        background-color: var(--bg-tertiary);
        border-color: var(--border-color);
        color: var(--text-primary);
    }

    .dark-mode .search-input:focus,
    .dark-mode .filter-select:focus {
        border-color: var(--accent-color);
        box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
    }

    .dark-mode .search-input::placeholder {
        color: var(--text-tertiary);
    }

    /* 네비게이션 다크모드 */
    .dark-mode .nav-item {
        color: var(--text-secondary);
    }

    .dark-mode .nav-item:hover {
        background-color: var(--bg-tertiary);
        color: var(--text-primary);
    }

    .dark-mode .nav-item.active {
        background-color: var(--accent-color);
        color: white;
    }

    .dark-mode .nav-badge {
        background-color: var(--bg-tertiary);
        color: var(--text-primary);
    }

    .dark-mode .nav-item.active .nav-badge {
        background-color: rgba(255, 255, 255, 0.2);
        color: white;
    }

    /* 사용자 정보 다크모드 */
    .dark-mode .user-name {
        color: var(--text-primary);
    }

    .dark-mode .user-status {
        color: var(--text-secondary);
    }

    /* 메인 콘텐츠 다크모드 */
    .dark-mode .app-container {
        background-color: var(--bg-primary);
    }

    .dark-mode .main-container {
        background: var(--gradient-primary);
        border: 1px solid var(--border-color);
    }

    .dark-mode .content-area {
        background-color: var(--bg-primary);
    }

    .dark-mode .dashboard-container {
        background-color: var(--bg-primary);
    }

    .dark-mode .section-card {
        background: var(--gradient-secondary);
        border: 1px solid var(--border-color);
        box-shadow: 0 4px 12px var(--shadow);
    }

    .dark-mode .section-title {
        color: var(--text-primary);
    }

    .dark-mode .section-subtitle {
        color: var(--text-secondary);
    }

    .dark-mode .content-card {
        background: var(--gradient-secondary);
        border: 1px solid var(--border-color);
        box-shadow: 0 2px 8px var(--shadow-light);
    }

    .dark-mode .content-card:hover {
        box-shadow: 0 6px 16px var(--shadow);
        border-color: var(--accent-color);
    }

    /* 카드 다크모드 */
    .dark-mode .word-card,
    .dark-mode .sentence-card {
        background: var(--gradient-secondary);
        border: 1px solid var(--border-color);
        color: var(--text-primary);
        box-shadow: 0 2px 8px var(--shadow-light);
    }

    .dark-mode .word-card:hover,
    .dark-mode .sentence-card:hover {
        background: var(--bg-tertiary);
        box-shadow: 0 6px 16px var(--shadow);
        border-color: var(--accent-color);
        transform: translateY(-2px);
    }

    .dark-mode .word-english {
        color: var(--text-primary);
        font-weight: 600;
    }

    .dark-mode .word-korean,
    .dark-mode .sentence-korean {
        color: var(--text-secondary);
    }

    .dark-mode .word-pronunciation {
        color: var(--accent-secondary);
    }

    .dark-mode .card-grid {
        gap: 20px;
    }

    .dark-mode .empty-state {
        background: var(--bg-secondary);
        color: var(--text-secondary);
        border: 2px dashed var(--border-color);
    }

    /* 헤더 다크모드 */
    .dark-mode .header {
        background: var(--gradient-primary);
        border-bottom: 1px solid var(--border-color);
        box-shadow: 0 2px 8px var(--shadow-light);
    }

    .dark-mode .header-title {
        color: var(--text-primary);
    }

    .dark-mode .header-subtitle {
        color: var(--text-secondary);
    }

    .dark-mode .coin-display {
        background: var(--gradient-secondary);
        color: var(--text-primary);
        border: 1px solid var(--border-color);
        box-shadow: 0 2px 4px var(--shadow-light);
    }

    .dark-mode .coin-icon {
        color: var(--accent-tertiary);
    }

    .dark-mode .progress-bar {
        background-color: var(--bg-tertiary);
    }

    .dark-mode .progress-fill {
        background: linear-gradient(90deg, var(--accent-color) 0%, var(--accent-secondary) 100%);
    }

    /* 버튼 다크모드 */
    .dark-mode .start-button,
    .dark-mode .btn-primary {
        background: linear-gradient(135deg, var(--accent-color) 0%, var(--accent-hover) 100%);
        color: white;
        border: none;
        box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
    }

    .dark-mode .start-button:hover,
    .dark-mode .btn-primary:hover {
        background: linear-gradient(135deg, var(--accent-hover) 0%, var(--accent-color) 100%);
        box-shadow: 0 4px 12px rgba(76, 175, 80, 0.4);
        transform: translateY(-1px);
    }

    .dark-mode .btn-secondary {
        background: var(--bg-tertiary);
        color: var(--text-primary);
        border: 1px solid var(--border-color);
    }

    .dark-mode .btn-secondary:hover {
        background: var(--bg-quaternary);
        border-color: var(--accent-color);
    }

    .dark-mode .icon-button {
        background: var(--bg-tertiary);
        color: var(--text-secondary);
        border: 1px solid var(--border-color);
    }

    .dark-mode .icon-button:hover {
        background: var(--accent-color);
        color: white;
    }

    /* 관리자 페이지 다크모드 */
    .dark-mode .tab-item {
        background-color: var(--bg-tertiary);
        color: var(--text-secondary);
        border-color: var(--border-color);
    }

    .dark-mode .tab-item.active {
        background-color: var(--accent-color);
        color: white;
    }

    .dark-mode .tab-content {
        background-color: var(--bg-secondary);
        border-color: var(--border-color);
    }

    .dark-mode .admin-form input,
    .dark-mode .admin-form textarea,
    .dark-mode .admin-form select {
        background-color: var(--bg-tertiary);
        border-color: var(--border-color);
        color: var(--text-primary);
    }

    .dark-mode .admin-form input:focus,
    .dark-mode .admin-form textarea:focus,
    .dark-mode .admin-form select:focus {
        border-color: var(--accent-color);
        box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
    }

    /* 토스트 알림 다크모드 */
    .dark-mode .toast {
        background-color: var(--bg-secondary);
        color: var(--text-primary);
        border: 1px solid var(--border-color);
    }

    .dark-mode .toast-title {
        color: var(--text-primary);
    }

    .dark-mode .toast-description {
        color: var(--text-secondary);
    }

    /* 스크롤바 다크모드 */
    .dark-mode ::-webkit-scrollbar {
        width: 8px;
    }

    .dark-mode ::-webkit-scrollbar-track {
        background: var(--bg-tertiary);
    }

    .dark-mode ::-webkit-scrollbar-thumb {
        background: var(--text-tertiary);
        border-radius: 4px;
    }

    .dark-mode ::-webkit-scrollbar-thumb:hover {
        background: var(--text-secondary);
    }

    /* 통계 및 대시보드 다크모드 */
    .dark-mode .stat-card {
        background: var(--gradient-secondary);
        border: 1px solid var(--border-color);
        box-shadow: 0 4px 12px var(--shadow);
    }

    .dark-mode .stat-card:hover {
        box-shadow: 0 6px 16px var(--shadow);
        transform: translateY(-2px);
    }

    .dark-mode .stat-number {
        color: var(--text-primary);
    }

    .dark-mode .stat-label {
        color: var(--text-secondary);
    }

    .dark-mode .dashboard-header h1 {
        color: var(--text-primary);
    }

    .dark-mode .dashboard-header p {
        color: var(--text-secondary);
    }

    .dark-mode .level-indicator {
        background: var(--gradient-secondary);
        border: 1px solid var(--border-color);
    }

    .dark-mode .level-text {
        color: var(--text-primary);
    }

    .dark-mode .level-progress {
        color: var(--text-secondary);
    }

    /* 학습 카드 특별 스타일 */
    .dark-mode .learning-card {
        background: var(--gradient-secondary);
        border: 1px solid var(--border-color);
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .dark-mode .learning-card:hover {
        background: var(--bg-tertiary);
        border-color: var(--accent-color);
        box-shadow: 0 8px 24px var(--shadow);
        transform: translateY(-4px) scale(1.02);
    }

    .dark-mode .learning-card.completed {
        background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(76, 175, 80, 0.05) 100%);
        border-color: var(--accent-color);
    }

    /* 애니메이션 */
    * {
        transition: background-color 0.3s ease, 
                   color 0.3s ease, 
                   border-color 0.3s ease,
                   box-shadow 0.3s ease,
                   transform 0.3s ease;
    }

    /* 테마 토글 버튼 애니메이션 */
    .theme-toggle {
        transition: all 0.2s ease;
    }

    .theme-toggle:active {
        transform: scale(0.95);
    }
`;

// 스타일 추가
const styleElement = document.createElement('style');
styleElement.textContent = darkModeStyles;
document.head.appendChild(styleElement);

// 전역 다크모드 관리자 인스턴스
window.darkModeManager = null;

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', () => {
    if (!window.darkModeManager) {
        window.darkModeManager = new DarkModeManager();
    }
});

// 다크모드 수동 토글 함수 (디버깅용)
window.toggleDarkMode = () => {
    if (window.darkModeManager) {
        window.darkModeManager.toggleDarkMode();
    }
};
