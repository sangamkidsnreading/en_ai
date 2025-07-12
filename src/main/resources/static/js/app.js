// 키리보카 앱 초기화 - 이벤트 충돌 방지 버전
class KiribocaApp {
    constructor() {
        this.currentPage = 'learning';
        this.isLoading = false;
        this.isPlaying = false;
        this.currentPlayingId = null;
        this.isRecording = false;
        this.recordingSentenceId = null;
        this.mediaRecorder = null;
        this.recordedAudios = {};
        this.activeSection = 'words';
        this.currentLevel = 1;
        this.currentDay = 1;
        this.totalCoins = 0;
        this.wordsLearned = 0;
        this.sentencesLearned = 0;
        this.learnedWords = new Set();
        this.learnedSentences = new Set();
        this.favoriteWords = new Set();
        this.eventListenersAdded = false;
        this.init();
    }

    // 앱 초기화
    async init() {
        console.log('🚀 키리보카 앱 시작');

        try {
            // 컴포넌트 로드
            await this.loadComponents();

            // IntegratedLearningManager가 이미 존재하는지 확인
            if (window.integratedLearningManager) {
                console.log('🔄 IntegratedLearningManager 이미 존재 - 이벤트 설정 스킵');
                this.hideLoading();
                return;
            }

            // 이벤트 리스너 설정 (중복 방지)
            if (!this.eventListenersAdded) {
                this.setupEventListeners();
                this.eventListenersAdded = true;
            }

            // 로딩 완료
            this.hideLoading();

            console.log('✅ 키리보카 앱 로드 완료');
        } catch (error) {
            console.error('❌ 앱 로드 중 오류:', error);
            this.showError('앱을 로드하는 중 오류가 발생했습니다.');
        }
    }

    // 컴포넌트 로드
    async loadComponents() {
        console.log('📦 컴포넌트 로드 시작');

        const loadPromises = [
            this.loadComponent('sidebar', 'components/sidebar.html'),
            this.loadComponent('main-content', 'components/main-content.html')
        ];

        await Promise.all(loadPromises);
        console.log('✅ 모든 컴포넌트 로드 완료');
    }

    // 개별 컴포넌트 로드
    async loadComponent(containerId, filePath) {
        try {
            const response = await fetch(filePath);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const html = await response.text();
            const container = document.getElementById(`${containerId}-container`);

            if (container) {
                container.innerHTML = html;
                console.log(`✅ ${containerId} 컴포넌트 로드 완료`);
            } else {
                console.warn(`⚠️ ${containerId}-container를 찾을 수 없습니다.`);
            }
        } catch (error) {
            console.error(`❌ ${containerId} 컴포넌트 로드 실패:`, error);
            throw error;
        }
    }

    // 특정 페이지 컴포넌트 로드
    async loadPageComponent(pageName) {
        try {
            const response = await fetch(`components/${pageName}.html`);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const html = await response.text();
            const container = document.getElementById('main-content-container');

            if (container) {
                container.innerHTML = html;
                console.log(`✅ ${pageName} 페이지 로드 완료`);

                // 페이지별 초기화 함수 호출
                this.initializePage(pageName);
            }
        } catch (error) {
            console.error(`❌ ${pageName} 페이지 로드 실패:`, error);
            this.showError(`${pageName} 페이지를 로드할 수 없습니다.`);
        }
    }

    // 페이지별 초기화
    initializePage(pageName) {
        switch (pageName) {
            case 'dashboard':
                this.initializeDashboard();
                break;
            case 'profile':
                this.initializeProfile();
                break;
            case 'admin':
                this.initializeAdmin();
                break;
            default:
                break;
        }
    }

    // 대시보드 초기화
    initializeDashboard() {
        console.log('📊 대시보드 초기화');

        // 대시보드 특별 기능 초기화
        // 예: 차트 렌더링, 통계 업데이트 등
        this.updateDashboardStats();
    }

    // 대시보드 통계 업데이트
    updateDashboardStats() {
        // 통계 정보 업데이트
        const statsElements = {
            'words-learned': this.wordsLearned,
            'sentences-learned': this.sentencesLearned,
            'total-coins': this.totalCoins,
            'current-level': this.currentLevel,
            'current-day': this.currentDay
        };

        Object.entries(statsElements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });
    }

    // 관리자 페이지 초기화
    initializeAdmin() {
        console.log('⚙️ 관리자 페이지 초기화');

        // 관리자 페이지 탭 기능 초기화
        setTimeout(() => {
            this.setupAdminTabs();
        }, 100);
    }

    // 관리자 탭 기능 설정
    setupAdminTabs() {
        const tabItems = document.querySelectorAll('.tab-item');
        const tabContents = document.querySelectorAll('.tab-content');

        tabItems.forEach(tab => {
            tab.addEventListener('click', () => {
                // 모든 탭 비활성화
                tabItems.forEach(t => t.classList.remove('active'));
                tabContents.forEach(c => c.classList.remove('active'));

                // 클릭된 탭 활성화
                tab.classList.add('active');
                const tabId = tab.dataset.tab + '-tab';
                const targetContent = document.getElementById(tabId);
                if (targetContent) {
                    targetContent.classList.add('active');
                }
            });
        });

        console.log('📋 관리자 탭 기능 설정 완료');
    }

    // 프로필 페이지 초기화
    initializeProfile() {
        console.log('👤 프로필 페이지 초기화');

        // 프로필 페이지 특별 기능 초기화
        setTimeout(() => {
            this.setupProfileEvents();
        }, 100);
    }

    // 수동으로 admin.js 로드 시도
    loadAdminScriptManually() {
        console.log('🔄 수동으로 admin.js 스크립트 로드 시도');

        const script = document.createElement('script');
        script.src = '/js/admin.js';
        script.onload = () => {
            console.log('✅ admin.js 수동 로드 완료');
            if (typeof window.initAdminDashboard === 'function') {
                window.initAdminDashboard();
                console.log('✅ AdminDashboard 수동 초기화 완료');
            } else {
                console.error('❌ admin.js 로드되었지만 초기화 함수가 없습니다.');
            }
        };
        script.onerror = () => {
            console.error('❌ admin.js 수동 로드 실패');
            console.log('💡 페이지를 새로고침하거나 개발자에게 문의하세요.');
        };

        document.head.appendChild(script);
    }

    // 프로필 이벤트 설정
    setupProfileEvents() {
        // 토글 스위치 기능
        const toggleSwitches = document.querySelectorAll('.toggle-switch input');
        toggleSwitches.forEach(toggle => {
            toggle.addEventListener('change', (e) => {
                console.log(`설정 변경: ${e.target.id} = ${e.target.checked}`);
            });
        });

        // 버튼 클릭 이벤트
        const updateBtns = document.querySelectorAll('.update-btn, .avatar-btn, .password-update-btn');
        updateBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const btnText = e.target.textContent;
                this.showToast('기능 준비중', `${btnText} 기능은 준비중입니다.`);
            });
        });

        console.log('👤 프로필 이벤트 설정 완료');
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        console.log('🔗 이벤트 리스너 설정');

        // 페이지 로드 후 이벤트 설정 (setTimeout으로 DOM 요소 생성 대기)
        setTimeout(() => {
            // IntegratedLearningManager가 없는 경우에만 이벤트 설정
            if (!window.integratedLearningManager) {
                this.setupNavigationEvents();
                this.setupWordCardEvents();
                this.setupSentenceCardEvents();
                this.setupButtonEvents();
                // this.setupSearchEvents(); // 주석 처리 - 전역 클릭 이벤트 충돌 방지
                this.setupSectionToggleEvents();
                this.updateDisplay();
            }
        }, 200);
    }

    // 네비게이션 이벤트 설정
    setupNavigationEvents() {
        const navItems = document.querySelectorAll('.nav-item');

        navItems.forEach(item => {
            // 이미 이벤트 리스너가 설정된 경우 스킵
            if (item.hasAttribute('data-nav-listener')) {
                return;
            }

            item.addEventListener('click', (e) => {
                e.preventDefault();
                const page = item.dataset.page;
                if (page) {
                    this.navigateTo(page);
                }
            });

            // 플래그 설정
            item.setAttribute('data-nav-listener', 'true');

            // 호버 효과 추가
            item.addEventListener('mouseenter', () => {
                if (!item.classList.contains('active')) {
                    item.style.transform = 'scale(1.02)';
                }
            });

            item.addEventListener('mouseleave', () => {
                if (!item.classList.contains('active')) {
                    item.style.transform = 'scale(1)';
                }
            });
        });

        console.log('🧭 네비게이션 이벤트 설정 완료');
    }

    // 섹션 토글 이벤트 설정
    setupSectionToggleEvents() {
        const toggleButtons = document.querySelectorAll('.section-toggle-btn');

        toggleButtons.forEach(button => {
            // 이미 이벤트 리스너가 설정된 경우 스킵
            if (button.hasAttribute('data-toggle-listener')) {
                return;
            }

            button.addEventListener('click', () => {
                const section = button.dataset.section;
                this.switchSection(section);
            });

            // 플래그 설정
            button.setAttribute('data-toggle-listener', 'true');
        });

        console.log('🔄 섹션 토글 이벤트 설정 완료');
    }

    // 섹션 전환
    switchSection(section) {
        // 활성 버튼 변경
        document.querySelectorAll('.section-toggle-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        const targetButton = document.querySelector(`[data-section="${section}"]`);
        if (targetButton) {
            targetButton.classList.add('active');
        }

        // 섹션 표시/숨김
        document.querySelectorAll('.words-section, .sentences-section').forEach(sec => {
            sec.classList.remove('active');
        });
        const targetSection = document.getElementById(`${section}-section`);
        if (targetSection) {
            targetSection.classList.add('active');
        }

        this.activeSection = section;
        console.log(`🔄 섹션 전환: ${section}`);
    }

    // 단어 카드 이벤트 설정 - 중복 방지
    setupWordCardEvents() {
        const wordCards = document.querySelectorAll('.word-card');

        wordCards.forEach(card => {
            // 이미 이벤트 리스너가 설정된 경우 스킵
            if (card.hasAttribute('data-word-listener') || card.hasAttribute('data-protected')) {
                return;
            }

            // 카드 클릭 이벤트
            card.addEventListener('click', (e) => {
                if (!e.target.closest('.favorite-btn')) {
                    this.handleWordClick(card);
                }
            });

            // 즐겨찾기 버튼
            const favoriteBtn = card.querySelector('.favorite-btn');
            if (favoriteBtn) {
                favoriteBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.toggleWordFavorite(card);
                });
            }

            // 플래그 설정
            card.setAttribute('data-word-listener', 'true');
        });

        console.log('🎴 단어 카드 이벤트 설정 완료');
    }

    // 문장 카드 이벤트 설정 - 중복 방지
    setupSentenceCardEvents() {
        const sentenceCards = document.querySelectorAll('.sentence-card');

        sentenceCards.forEach(card => {
            // 이미 이벤트 리스너가 설정된 경우 스킵
            if (card.hasAttribute('data-sentence-listener') || card.hasAttribute('data-protected')) {
                return;
            }

            // 카드 클릭 이벤트
            card.addEventListener('click', (e) => {
                if (!e.target.closest('.record-btn')) {
                    this.handleSentenceClick(card);
                }
            });

            // 녹음 버튼
            const recordBtn = card.querySelector('.record-btn');
            if (recordBtn) {
                recordBtn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    this.handleSentenceRecording(card);
                });
            }

            // 플래그 설정
            card.setAttribute('data-sentence-listener', 'true');
        });

        console.log('📝 문장 카드 이벤트 설정 완료');
    }

    // 문장 클릭 처리
    handleSentenceClick(card) {
        const sentenceId = card.dataset.sentenceId;
        const sentenceText = card.querySelector('.sentence-text').textContent;

        // TTS 재생
        this.speakText(sentenceText);

        // 시각적 피드백
        card.classList.add('playing');
        this.currentPlayingId = sentenceId;

        setTimeout(() => {
            card.classList.remove('playing');
            if (this.currentPlayingId === sentenceId) {
                this.currentPlayingId = null;
            }
        }, 2000);

        // 학습 완료 처리
        if (!this.learnedSentences.has(sentenceId)) {
            this.learnedSentences.add(sentenceId);
            this.sentencesLearned++;
            this.totalCoins += 3;

            // 학습 완료 표시
            card.classList.add('learned');
            const learnedCheck = card.querySelector('.learned-check');
            if (learnedCheck) {
                learnedCheck.classList.remove('hidden');
            }

            this.updateDisplay();
            this.showToast('문장 학습 완료', `"${sentenceText}" 학습 완료! +3 코인`);
        }
    }

    // 즐겨찾기 토글
    toggleWordFavorite(card) {
        const wordId = card.dataset.wordId;
        const favoriteBtn = card.querySelector('.favorite-btn');

        if (this.favoriteWords.has(wordId)) {
            this.favoriteWords.delete(wordId);
            favoriteBtn.textContent = '🤍';
        } else {
            this.favoriteWords.add(wordId);
            favoriteBtn.textContent = '❤️';
        }

        // 애니메이션 효과
        favoriteBtn.style.transform = 'scale(1.3)';
        setTimeout(() => {
            favoriteBtn.style.transform = 'scale(1)';
        }, 200);

        console.log(`${this.favoriteWords.has(wordId) ? '💖' : '💔'} 즐겨찾기 ${this.favoriteWords.has(wordId) ? '추가' : '해제'}`);
    }

    // 문장 녹음 처리
    async handleSentenceRecording(card) {
        const sentenceId = card.dataset.sentenceId;
        const recordBtn = card.querySelector('.record-btn');
        const recordIcon = recordBtn.querySelector('.record-icon');

        if (this.isRecording && this.recordingSentenceId === sentenceId) {
            // 녹음 중단
            if (this.mediaRecorder) {
                this.mediaRecorder.stop();
                this.isRecording = false;
                this.recordingSentenceId = null;
                recordBtn.classList.remove('recording');
                recordIcon.textContent = '🎤';
                this.setMediaRecorder(null);
            }
            return;
        }

        // 이미 녹음된 파일이 있으면 재생
        if (this.recordedAudios[sentenceId] && !this.isRecording) {
            const audio = new Audio(this.recordedAudios[sentenceId]);
            audio.play();
            this.showToast('내 녹음 재생', '녹음된 음성을 재생합니다.');
            return;
        }

        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            const recorder = new MediaRecorder(stream);
            const audioChunks = [];

            recorder.ondataavailable = (event) => {
                audioChunks.push(event.data);
            };

            recorder.onstop = () => {
                const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
                const audioUrl = URL.createObjectURL(audioBlob);

                this.recordedAudios[sentenceId] = audioUrl;

                // 녹음 완료 표시
                const recordedIndicator = card.querySelector('.recorded-indicator');
                if (recordedIndicator) {
                    recordedIndicator.classList.remove('hidden');
                }
                recordBtn.classList.add('recorded');
                recordIcon.textContent = '▶️';

                const sentenceText = card.querySelector('.sentence-text').textContent;
                this.showToast('녹음 완료', `"${sentenceText}" 녹음이 완료되었습니다.`);

                stream.getTracks().forEach(track => track.stop());
            };

            recorder.start();
            this.setMediaRecorder(recorder);
            this.isRecording = true;
            this.recordingSentenceId = sentenceId;
            recordBtn.classList.add('recording');
            recordIcon.textContent = '⏹️';

            this.showToast('녹음 시작', '녹음을 시작합니다. 다시 클릭하면 중단됩니다.');

        } catch (error) {
            console.error('녹음 시작 오류:', error);
            this.showToast('녹음 오류', '마이크 접근 권한을 확인해주세요.');
        }
    }

    // 버튼 이벤트 설정
    setupButtonEvents() {
        const startButton = document.getElementById('start-learning-btn');

        if (startButton && !startButton.hasAttribute('data-start-listener')) {
            startButton.addEventListener('click', () => {
                this.handleStartLearning();
            });
            startButton.setAttribute('data-start-listener', 'true');
        }

        console.log('🔘 버튼 이벤트 설정 완료');
    }

    // 학습 시작/중단
    handleStartLearning() {
        const startButton = document.getElementById('start-learning-btn');
        const startIcon = startButton.querySelector('.start-icon');
        const startText = startButton.querySelector('.start-text');

        if (this.isPlaying) {
            // 학습 중단
            this.isPlaying = false;
            this.currentPlayingId = null;
            speechSynthesis.cancel();

            startButton.classList.remove('playing');
            startIcon.textContent = '🎯';
            startText.textContent = 'Start';

            /*this.showToast('학습 중단', '학습을 중단했습니다.');*/
            return;
        }

        // 학습 시작
        const items = this.activeSection === 'words'
            ? document.querySelectorAll('.word-card')
            : document.querySelectorAll('.sentence-card');

        if (items.length === 0) return;

        this.isPlaying = true;
        startButton.classList.add('playing');
        startIcon.textContent = '⏹️';
        startText.textContent = 'Stop';

        let currentIndex = 0;
        let repeatCount = 0;
        const maxRepeats = 3;

        const playNext = () => {
            if (!this.isPlaying || currentIndex >= items.length) {
                this.isPlaying = false;
                startButton.classList.remove('playing');
                startIcon.textContent = '🎯';
                startText.textContent = 'Start';

                if (currentIndex >= items.length) {
                    this.showToast('학습 완료!', `${items.length}개의 ${this.activeSection === 'words' ? '단어' : '문장'}를 모두 학습했습니다.`);
                }
                return;
            }

            const item = items[currentIndex];
            const id = item.dataset.wordId || item.dataset.sentenceId;
            this.currentPlayingId = id;
            item.classList.add('playing');

            const text = this.activeSection === 'words'
                ? item.querySelector('.word-english').textContent
                : item.querySelector('.sentence-text').textContent;

            const utterance = new SpeechSynthesisUtterance(text);
            utterance.rate = 0.8;
            utterance.lang = 'en-US';

            utterance.onend = () => {
                item.classList.remove('playing');
                repeatCount++;

                if (repeatCount < maxRepeats) {
                    setTimeout(playNext, 300);
                } else {
                    // 학습 완료 처리
                    if (this.activeSection === 'words') {
                        this.handleWordClick(item);
                    } else {
                        this.handleSentenceClick(item);
                    }

                    repeatCount = 0;
                    currentIndex++;
                    setTimeout(playNext, 500);
                }
            };

            speechSynthesis.speak(utterance);
        };

        playNext();
        this.showToast('학습 시작', '자동 학습을 시작합니다.');
    }

    // 검색 이벤트 설정
    setupSearchEvents() {
        const searchInput = document.querySelector('.search-input');

        if (searchInput && !searchInput.hasAttribute('data-search-listener')) {
            searchInput.addEventListener('input', (e) => {
                this.handleSearch(e.target.value);
            });
            searchInput.setAttribute('data-search-listener', 'true');
        }

        console.log('🔍 검색 이벤트 설정 완료');
    }

    // 검색 처리
    handleSearch(query) {
        console.log(`🔍 검색: ${query}`);

        if (query.length < 2) {
            // 검색어가 짧으면 모든 카드 표시
            document.querySelectorAll('.word-card, .sentence-card').forEach(card => {
                card.style.display = 'block';
                card.style.visibility = 'visible';
            });
            return;
        }

        const wordCards = document.querySelectorAll('.word-card');
        const sentenceCards = document.querySelectorAll('.sentence-card');

        wordCards.forEach(card => {
            const english = card.querySelector('.word-english')?.textContent?.toLowerCase() || '';
            const korean = card.querySelector('.word-korean')?.textContent?.toLowerCase() || '';

            if (english.includes(query.toLowerCase()) || korean.includes(query.toLowerCase())) {
                card.style.display = 'block';
                card.style.visibility = 'visible';
            } else {
                // display none 대신 visibility hidden 사용 (더 안전)
                card.style.visibility = 'hidden';
                // card.style.display = 'none'; // 주석 처리
            }
        });

        sentenceCards.forEach(card => {
            const text = card.querySelector('.sentence-text')?.textContent?.toLowerCase() || '';

            if (text.includes(query.toLowerCase())) {
                card.style.display = 'block';
                card.style.visibility = 'visible';
            } else {
                // display none 대신 visibility hidden 사용 (더 안전)
                card.style.visibility = 'hidden';
                // card.style.display = 'none'; // 주석 처리
            }
        });
    }

    // 페이지 네비게이션
    async navigateTo(page) {
        console.log(`📄 페이지 이동: ${page}`);

        // 현재 활성 메뉴 제거
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
        });

        // 새로운 활성 메뉴 설정
        const newActiveItem = document.querySelector(`[data-page="${page}"]`);
        if (newActiveItem) {
            newActiveItem.classList.add('active');

            // 학습하기 메뉴인 경우 특별한 클래스 추가
            if (page === 'learning') {
                newActiveItem.classList.add('learning-item');
            }
        }

        // 페이지별 로직 처리
        switch (page) {
            case 'learning':
                await this.showStudyPage();
                break;
            case 'dashboard':
                await this.showDashboard();
                break;
            case 'profile':
                await this.showProfile();
                break;
            case 'admin':
                await this.showAdmin();
                break;
            case 'logout':
                this.handleLogout();
                break;
        }

        this.currentPage = page;
        // 관리자 페이지로 전환된 경우 AdminDashboard 초기화
        if (page === 'admin') {
            console.log('🔧 관리자 페이지 진입 - AdminDashboard 초기화 예약');

            // 더 견고한 admin.js 로드 확인 및 초기화
            const initAdminWithRetry = (attempt = 1, maxAttempts = 5) => {
                console.log(`🔧 AdminDashboard 초기화 시도 #${attempt}`);

                if (typeof window.initAdminDashboard === 'function') {
                    try {
                        window.initAdminDashboard();
                        console.log('✅ AdminDashboard 초기화 완료');
                        return;
                    } catch (error) {
                        console.error('❌ AdminDashboard 초기화 중 오류:', error);
                    }
                }

                if (attempt < maxAttempts) {
                    console.log(`🔄 admin.js 로드 재시도 중... (${attempt}/${maxAttempts})`);
                    setTimeout(() => {
                        initAdminWithRetry(attempt + 1, maxAttempts);
                    }, 500 * attempt); // 점진적 지연
                } else {
                    console.error('❌ admin.js 로드 최종 실패');
                    console.log('🔍 현재 window 객체의 admin 관련 속성들:',
                        Object.keys(window).filter(key => 
                            key.toLowerCase().includes('admin') || 
                            key.includes('Admin')
                        ));

                    // 수동 스크립트 로드 시도
                    this.loadAdminScriptManually();
                }
            };

            // 즉시 시도하고, 실패하면 재시도
            setTimeout(() => initAdminWithRetry(), 100);
        }
    }

    // 텍스트 읽기 (TTS)
    speakText(text) {
        if ('speechSynthesis' in window) {
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'en-US';
            utterance.rate = 0.8;
            speechSynthesis.speak(utterance);
        }
    }

    // 디스플레이 업데이트
    updateDisplay() {
        // 헤더 정보 업데이트
        const currentLevelEl = document.getElementById('current-level');
        const currentDayEl = document.getElementById('current-day');
        const wordsLearnedEl = document.getElementById('words-learned');
        const sentencesLearnedEl = document.getElementById('sentences-learned');
        const totalCoinsEl = document.getElementById('total-coins');
        const displayLevelEl = document.getElementById('display-level');
        const displayDayEl = document.getElementById('display-day');

        if (currentLevelEl) currentLevelEl.textContent = this.currentLevel;
        if (currentDayEl) currentDayEl.textContent = this.currentDay;
        if (wordsLearnedEl) wordsLearnedEl.textContent = this.wordsLearned;
        if (sentencesLearnedEl) sentencesLearnedEl.textContent = this.sentencesLearned;
        if (totalCoinsEl) totalCoinsEl.textContent = this.totalCoins;
        if (displayLevelEl) displayLevelEl.textContent = this.currentLevel;
        if (displayDayEl) displayDayEl.textContent = this.currentDay;
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

    // 페이지별 표시 함수들
    async showStudyPage() {
        console.log('📚 학습 페이지 표시');
        await this.loadPageComponent('main-content');
        this.showToast('학습하기', '학습 페이지입니다.');
        // 학습 페이지 특별 이벤트 재설정
        setTimeout(() => {
            this.setupWordCardEvents();
            this.setupSentenceCardEvents();
            this.setupButtonEvents();
            this.setupSectionToggleEvents();
        }, 100);
    }

    async showDashboard() {
        console.log('📊 대시보드 표시');
        await this.loadPageComponent('dashboard');
        this.showToast('대시보드', '대시보드를 로드했습니다!');
    }

    async showProfile() {
        console.log('👤 프로필 표시');
        await this.loadPageComponent('profile');
        this.showToast('프로필', '프로필 페이지를 로드했습니다!');
    }

    async showAdmin() {
        console.log('⚙️ 관리자 페이지 표시');
        await this.loadPageComponent('admin');
        this.showToast('관리자', '관리자 페이지를 로드했습니다!');
    }

    handleLogout() {
        console.log('🚪 로그아웃 처리');
        if (confirm('정말 로그아웃하시겠습니까?')) {
            this.showToast('로그아웃', '로그아웃되었습니다!');
            // 실제로는 서버에 로그아웃 요청 전송
        }
    }

    // 유틸리티 함수들
    hideLoading() {
        const loading = document.getElementById('loading');
        const appContainer = document.querySelector('.app-container');

        if (loading) {
            loading.style.opacity = '0';
            setTimeout(() => {
                loading.style.display = 'none';
                if (appContainer) {
                    appContainer.classList.add('loaded');
                }
            }, 300);
        }
    }

    showError(message) {
        console.error(message);
        this.showToast('오류', message);
    }

    setMediaRecorder(recorder) {
        this.mediaRecorder = recorder;
    }
}

// CSS 애니메이션 추가
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }

    @keyframes fadeInOut {
        0% { opacity: 0; transform: translateX(-50%) translateY(-10px); }
        50% { opacity: 1; transform: translateX(-50%) translateY(0); }
        100% { opacity: 0; transform: translateX(-50%) translateY(-10px); }
    }

    .toast {
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        padding: 15px 20px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 1000;
        animation: slideIn 0.3s ease-out;
    }

    .toast-title {
        font-weight: 600;
        color: #333;
        margin-bottom: 5px;
    }

    .toast-description {
        font-size: 14px;
        color: #666;
    }
`;
document.head.appendChild(style);

// 앱 초기화 (DOM 로드 완료 후)
document.addEventListener('DOMContentLoaded', () => {
    // IntegratedLearningManager가 없는 경우에만 KiribocaApp 초기화
    if (!window.integratedLearningManager) {
        window.kiribocaApp = new KiribocaApp();
    } else {
        console.log('🔄 IntegratedLearningManager 존재 - KiribocaApp 초기화 스킵');
    }
});

// 개발자 도구용 디버깅 함수
window.debugKiriboca = {
    reloadComponents: () => window.kiribocaApp?.loadComponents(),
    navigateTo: (page) => window.kiribocaApp?.navigateTo(page),
    showNotification: (msg, type) => window.kiribocaApp?.showNotification(msg, type)
};