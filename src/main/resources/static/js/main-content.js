// 중복 선언 방지
if (typeof window.EnhancedIntegratedLearningManager !== 'undefined') {
    console.log('🔄 기존 EnhancedIntegratedLearningManager 제거');
    delete window.EnhancedIntegratedLearningManager;
}

// 향상된 통합 학습 관리자 - 코인 시스템 완전 통합
class EnhancedIntegratedLearningManager {
    constructor() {
        this.currentLevel = 1;
        this.currentDay = 1;
        this.words = [];
        this.sentences = [];
        this.stats = {};
        this.coins = {};
        this.isInitialized = false;
        this.eventListenersAdded = false;
        this.completedWords = new Set();
        this.completedSentences = new Set();
        this.isPlaying = false;
        this.currentPlayback = null;
        this.observer = null;

        this.init();
    }

    // 초기화
    async init() {
        try {
            console.log('🚀 향상된 통합 학습 관리자 초기화 시작');

            // 기존 KiribocaApp과의 충돌 방지를 위해 더 오래 대기
            await new Promise(resolve => setTimeout(resolve, 1000));

            // 데이터 로드
            await this.loadLearningData();

            // 이벤트 설정 (중복 방지)
            if (!this.eventListenersAdded) {
                this.setupEvents();
                this.eventListenersAdded = true;
            }
            
            // sidebar 연동 필터 이벤트 추가
            try {
                this.setupSidebarFilters();
            } catch (sidebarError) {
                console.warn('⚠️ 사이드바 필터 설정 실패:', sidebarError);
            }
            
            this.updateHeader();

            // UI 업데이트
            this.updateUI();

            // 전역 클릭 이벤트 차단 (더 늦게 실행)
            setTimeout(() => {
                // this.setupGlobalEventProtection(); // 주석 처리 또는 삭제
            }, 500);

            this.isInitialized = true;
            console.log('✅ 향상된 통합 학습 관리자 초기화 완료');

        } catch (error) {
            console.error('❌ 초기화 실패:', error);
            console.error('❌ 오류 상세:', error.message, error.stack);
            this.showError('데이터를 불러오는데 실패했습니다: ' + (error.message || '알 수 없는 오류'));
        }
    }

    // 전역 이벤트 보호 설정 - 강화된 버전
    // setupGlobalEventProtection 함수 전체 삭제
    // (함수 정의와 내부 document.addEventListener('click', ...) 포함)
    // 만약 호출부가 있다면 아래처럼 주석 처리
    // this.setupGlobalEventProtection();

    // 학습 데이터 로드
    async loadLearningData() {
        try {
            const [wordsResponse, sentencesResponse, statsResponse] = await Promise.all([
                this.fetchWords(),
                this.fetchSentences(),
                this.fetchStats()
            ]);

            this.words = wordsResponse;
            this.sentences = sentencesResponse;
            this.stats = statsResponse;

            console.log('📚 데이터 로드 완료:', {
                words: this.words.length,
                sentences: this.sentences.length,
                stats: this.stats
            });

            // 코인 정보 로드
            await this.loadCoins();

            // 데이터를 받아온 후 HTML 업데이트
            this.renderWordsToHTML();
            this.renderSentencesToHTML();

        } catch (error) {
            console.error('데이터 로드 실패:', error);
            this.stats = {
                completedWords: 0,
                totalWords: 1,
                completedSentences: 0,
                totalSentences: 0,
                coinsEarned: 0
            };
        }
    }

    // 단어를 HTML로 렌더링
    renderWordsToHTML() {
        const wordsGrid = document.querySelector('.words-grid');
        if (!wordsGrid) {
            console.warn('⚠️ words-grid 요소를 찾을 수 없습니다.');
            return;
        }

        wordsGrid.innerHTML = '';

        if (this.words.length === 0) {
            wordsGrid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">
                    <div style="font-size: 48px; margin-bottom: 16px;">📚</div>
                    <div style="font-size: 18px; margin-bottom: 8px;">이 레벨/Day에는 단어가 없습니다</div>
                    <div style="font-size: 14px;">다른 레벨이나 Day를 선택해보세요!</div>
                </div>
            `;
            hideLoadingSpinner();
            console.log('📝 단어 데이터가 없어서 빈 상태를 표시합니다.');
            return;
        }

        this.words.forEach((word, index) => {
            const wordCard = document.createElement('div');
            wordCard.className = 'word-card';
            wordCard.setAttribute('data-word-id', word.id);
            wordCard.setAttribute('data-audio-file', word.audioUrl); // 추가: audio_file 속성

            console.log(`📝 단어 카드 생성: ${word.text}, audioUrl: ${word.audioUrl}`);

            wordCard.innerHTML = `
                <div class="word-favorite">♡</div>
                <div class="word-english">${word.text || 'Word'}</div>
                <div class="word-korean">${word.meaning || '의미'}</div>
               
            `;

            wordsGrid.appendChild(wordCard);
        });
        // 카드 display/visibility 강제 복구
        setTimeout(() => {
            document.querySelectorAll('.word-card').forEach(card => {
                card.style.display = 'block';
                card.style.visibility = 'visible';
            });
            // 동적 렌더링 후 반드시 이벤트 재설정
            this.setupWordCardEvents();
        }, 0);
        hideLoadingSpinner();
        console.log(`📝 ${this.words.length}개의 단어 카드가 렌더링되었습니다.`);
    }

    // 문장을 HTML로 렌더링
    renderSentencesToHTML() {
        const sentencesGrid = document.querySelector('.sentences-grid');
        if (!sentencesGrid) {
            console.warn('⚠️ sentences-grid 요소를 찾을 수 없습니다.');
            return;
        }

        sentencesGrid.innerHTML = '';

        if (this.sentences.length === 0) {
            sentencesGrid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">
                    <div style="font-size: 48px; margin-bottom: 16px;">📝</div>
                    <div style="font-size: 18px; margin-bottom: 8px;">아직 문장이 없습니다</div>
                    <div style="font-size: 14px;">곧 추가될 예정입니다!</div>
                </div>
            `;
            hideLoadingSpinner();
            return;
        }

        this.sentences.forEach((sentence, index) => {
            const sentenceCard = document.createElement('div');
            sentenceCard.className = 'sentence-card';
            sentenceCard.setAttribute('data-sentence-id', sentence.id);
            sentenceCard.setAttribute('data-audio-file', sentence.audioUrl); // 추가: audio_file 속성

            // 이벤트 바인딩 전에 반드시 속성 제거
            sentenceCard.removeAttribute('data-integrated-listener');
            sentenceCard.removeAttribute('data-protected');

            const englishText = sentence.english || sentence.text || 'Sample sentence';
            const koreanText = sentence.korean || sentence.meaning || sentence.translation || '';

            sentenceCard.innerHTML = `
                <div class="sentence-content">
                    <div class="sentence-text">${englishText}</div>
                    ${koreanText ? `<div class="sentence-korean">${koreanText}</div>` : ''}
                </div>
              
            `;

            sentencesGrid.appendChild(sentenceCard);
        });
        // 카드 display/visibility 강제 복구
        setTimeout(() => {
            document.querySelectorAll('.sentence-card').forEach(card => {
                card.style.display = 'block';
                card.style.visibility = 'visible';
            });
            // 동적 렌더링 후 반드시 이벤트 재설정
            this.setupSentenceCardEvents();
        }, 0);
        hideLoadingSpinner();
        console.log(`📝 ${this.sentences.length}개의 문장 카드가 동적으로 렌더링되었습니다.`);
    }

    // 백엔드 API 호출 메서드들
    async fetchWords() {
        try {
            const response = await fetch(`/learning/api/words?level=${this.currentLevel}&day=${this.currentDay}`);
            if (!response.ok) throw new Error('단어 데이터 로드 실패');
            const data = await response.json();
            console.log('📝 단어 데이터 로드됨:', data.length, '개');
            console.log('📝 단어 데이터 상세:', data);
            return data;
        } catch (error) {
            console.error('단어 API 호출 실패:', error);
            return [];
        }
    }

    async fetchSentences() {
        try {
            const response = await fetch(`/learning/api/sentences?level=${this.currentLevel}&day=${this.currentDay}`);
            if (!response.ok) throw new Error('문장 데이터 로드 실패');
            const data = await response.json();
            console.log('📝 문장 데이터 로드됨:', data.length, '개');
            return data;
        } catch (error) {
            console.error('문장 API 호출 실패:', error);
            return [];
        }
    }

    async fetchStats() {
        try {
            const response = await fetch(`/learning/api/stats/today?level=${this.currentLevel}&day=${this.currentDay}`);
            if (!response.ok) throw new Error('통계 데이터 로드 실패');
            return await response.json();
        } catch (error) {
            console.error('통계 API 호출 실패:', error);
            return {
                completedWords: 0,
                totalWords: 1,
                completedSentences: 0,
                totalSentences: 0,
                coinsEarned: 0
            };
        }
    }

    // 이벤트 설정
    setupEvents() {
        if (this.eventListenersAdded) {
            console.log('🔄 이벤트 리스너가 이미 설정되어 있음 - 스킵');
            return;
        }

        console.log('🔗 향상된 통합 학습 관리자 이벤트 설정');

        this.removeExistingEventListeners();
        this.setupWordCardEvents();
        this.setupSentenceCardEvents();
        this.setupStartButtonEvents();

        this.eventListenersAdded = true;
        console.log('🔗 이벤트 설정 완료');
    }

    removeExistingEventListeners() {
        console.log('🔧 기존 이벤트 리스너 정리 (충돌 방지)');

        const wordCards = document.querySelectorAll('.word-card');
        wordCards.forEach(card => {
            card.setAttribute('data-integrated-listener', 'true');
            card.setAttribute('data-protected', 'true');
        });

        const sentenceCards = document.querySelectorAll('.sentence-card');
        sentenceCards.forEach(card => {
            card.setAttribute('data-integrated-listener', 'true');
            card.setAttribute('data-protected', 'true');
        });
    }

    // 단어 카드 이벤트 설정
    setupWordCardEvents() {
        document.querySelectorAll('.word-card').forEach(card => {
            // 카드 전체 클릭 이벤트
            card.addEventListener('click', (e) => {
                // 사운드 버튼 클릭은 무시
                if (e.target.closest('.word-sound')) {
                    return false;
                }
                this.handleWordClick(card);
            });

            // 사운드 버튼 클릭 이벤트
            const soundBtn = card.querySelector('.word-sound');
            if (soundBtn) {
                soundBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    this.playWordAudio(card);
                    return false;
                }, { capture: true, passive: false });
            }
        });
    }

    // 문장 카드 이벤트 설정
    setupSentenceCardEvents() {
        const sentenceCards = document.querySelectorAll('.sentence-card');

        sentenceCards.forEach((card, index) => {
            // 항상 속성 제거 후 이벤트 바인딩
            card.removeAttribute('data-integrated-listener');
            card.removeAttribute('data-protected');

            if (card.getAttribute('data-integrated-listener') === 'true') {
                console.log('🔄 문장 카드 이벤트 리스너 이미 설정됨 - 스킵');
                return;
            }

            if (this.sentences[index]) {
                const sentence = this.sentences[index];
                card.setAttribute('data-sentence-id', sentence.id);

                const textEl = card.querySelector('.sentence-text');
                if (textEl && sentence.text) textEl.textContent = sentence.text;
            }

            card.setAttribute('data-integrated-listener', 'true');
            card.setAttribute('data-protected', 'true');

            const handleCardClick = (e) => {
                // 이미 처리된 이벤트는 무시
                if (e.defaultPrevented) return;

                e.preventDefault();
                e.stopPropagation();

                // 사운드 버튼 클릭은 무시
                if (e.target.closest('.sentence-sound')) {
                    return false;
                }

                console.log('📝 문장 카드 클릭됨');
                this.handleSentenceClick(card);
                return false;
            };

            // 카드 전체 영역에 클릭 이벤트 추가 (더 안전한 방식)
            card.addEventListener('click', handleCardClick, { capture: false, passive: false });

            const soundBtn = card.querySelector('.sentence-sound');
            if (soundBtn) {
                soundBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                    this.playSentenceAudio(card);
                    return false;
                }, { capture: true, passive: false });
            }

            ['touchstart', 'touchend', 'mouseup'].forEach(eventType => {
                card.addEventListener(eventType, (e) => {
                    e.stopPropagation();
                    e.stopImmediatePropagation();
                }, { capture: true, passive: false });
            });
        });
    }

    // Start 버튼 이벤트 설정
    setupStartButtonEvents() {
        const startButtons = document.querySelectorAll('.start-button');

        startButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();

                const studyArea = btn.closest('.study-area');
                const isWordsSection = studyArea && studyArea.querySelector('.word-card');

                if (this.isPlaying) {
                    // 현재 재생 중이면 중지
                    this.stopLearning();
                    btn.textContent = '🚀 Start';
                    btn.style.backgroundColor = '#007bff';
                } else {
                    // 재생 중이 아니면 시작
                if (isWordsSection) {
                    this.startWordsLearning();
                } else {
                    this.startSentencesLearning();
                    }
                    btn.textContent = '⏹️ Stop';
                    btn.style.backgroundColor = '#dc3545';
                }
                return false;
            }, { capture: true, passive: false });
        });
    }

    // 단어 클릭 처리 - 코인 시스템 통합
    async handleWordClick(card) {
        const wordId = card.getAttribute('data-word-id');
        const wordText = card.querySelector('.word-english')?.textContent;

        if (!wordText) return;

        console.log('🔤 단어 클릭:', wordText);

        // 시각적 피드백
        this.highlightCard(card);

        // 실제 오디오 파일 또는 TTS 음성 재생
        await this.playWordAudio(card);

        // 음성 재생 완료 후 코인 추가
        await this.addCoinAfterAudio('word', wordText);
    }

    // 음성 재생 완료 후 코인 추가 (비동기 처리로 속도 향상)
    async addCoinAfterAudio(type, text) {
        console.log('addCoinAfterAudio called', type, text);
        
        // 코인 API 호출을 비동기로 처리 (음성 재생 속도에 영향 없도록)
        Promise.resolve().then(async () => {
            try {
                // 코인 추가 API 호출
                const coinResult = type === 'word' ? 
                    await this.addWordCoins() : 
                    await this.addSentenceCoins();

                if (coinResult && coinResult.success) {
                    // 코인 정보 업데이트
                    if (coinResult.coinResult) {
                        this.coins = coinResult.coinResult;
                        this.updateCoinDisplay();
                    }
                    
                    // learning_settings에서 가져온 실제 코인 수량 사용
                    let coinAmount = '+1';
                    let coinCount = 1;
                    
                    if (type === 'word') {
                        coinCount = coinResult.wordCoins || 1;
                        coinAmount = `+${coinCount}`;
                    } else if (type === 'sentence') {
                        coinCount = coinResult.sentenceCoins || 3;
                        coinAmount = `+${coinCount}`;
                    }
                    
                    this.showCoinAnimation(coinAmount);
                    console.log(`🪙 ${type} 코인 획득 (설정값 기반):`, coinCount, '개');
                    /*this.showMessage(`"${text}" 학습 완료! 코인 ${coinCount}개 획득! 🪙`);*/
                }
                
                // 통계 새로고침도 비동기로 처리
                this.refreshStats();
            } catch (error) {
                console.error('❌ 코인 추가 실패:', error);
            }
        });
    }

    // 문장 클릭 처리 - 코인 시스템 통합
    async handleSentenceClick(card) {
        const sentenceId = card.getAttribute('data-sentence-id');
        const sentenceText = card.querySelector('.sentence-text')?.textContent;

        if (!sentenceText) return;

        console.log('📝 문장 클릭:', sentenceText.substring(0, 30) + '...');

        // 시각적 피드백
        this.highlightCard(card);

        // 실제 오디오 파일 또는 TTS 음성 재생
        await this.playSentenceAudio(card);

        // 음성 재생 완료 후 코인 추가
        await this.addCoinAfterAudio('sentence', sentenceText.substring(0, 20) + '...');
    }

    // 완료도 체크 후 보너스 지급
    async checkCompletionBonus() {
        const totalWords = this.words.length;
        const totalSentences = this.sentences.length;
        const completedWordsCount = this.completedWords.size;
        const completedSentencesCount = this.completedSentences.size;

        // 모든 학습을 완료했을 때 보너스 지급
        if (completedWordsCount >= totalWords && completedSentencesCount >= totalSentences) {
            try {
                const bonusResult = await this.addStreakBonus();
                if (bonusResult) {
                    this.coins = bonusResult;
                    this.updateCoinDisplay();
                    this.showCoinAnimation('+5 BONUS!');
                    /*this.showMessage('🎉 모든 학습 완료! 보너스 5코인 획득! 🎉');*/
                    console.log('🏆 완료 보너스 획득:', bonusResult);
                }
            } catch (error) {
                console.error('❌ 보너스 지급 실패:', error);
            }
        }
    }

    // 카드 강조 효과
    highlightCard(card) {
        card.style.transform = 'scale(1.05)';
        card.style.boxShadow = '0 4px 15px rgba(0,123,255,0.3)';
        card.style.zIndex = '1000';

        setTimeout(() => {
            card.style.transform = 'scale(1)';
            card.style.boxShadow = '';
            card.style.zIndex = '';
        }, 300);
    }

    // 🪙 코인 관련 API 메서드들

    // 코인 정보 로드
    async loadCoins() {
        try {
            const response = await fetch('/api/coins/user', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.ok) {
                this.coins = await response.json();
                this.updateCoinDisplay();
                console.log('🪙 코인 정보 로드:', this.coins);
            }
        } catch (error) {
            console.error('❌ 코인 로드 실패:', error);
        }
    }

    // 단어 완료 시 코인 추가
    async addWordCoins() {
        console.log('addWordCoins called');
        try {
            const response = await fetch('/learning/api/coins/word', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('❌ 단어 코인 추가 실패:', error);
        }
        return null;
    }

    // 문장 완료 시 코인 추가
    async addSentenceCoins() {
        try {
            const response = await fetch('/learning/api/coins/sentence', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('❌ 문장 코인 추가 실패:', error);
        }
        return null;
    }

    // 연속 학습 보너스 추가
    async addStreakBonus() {
        try {
            const response = await fetch('/learning/api/coins/streak', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include'
            });

            if (response.ok) {
                return await response.json();
            }
        } catch (error) {
            console.error('❌ 보너스 코인 추가 실패:', error);
        }
        return null;
    }

    // 코인 표시 업데이트
    updateCoinDisplay() {
        const coinNumberElement = document.querySelector('.coin-number');
        if (coinNumberElement && this.coins) {
            const oldValue = parseInt(coinNumberElement.textContent) || 0;
            const newValue = this.coins.dailyCoins || this.coins.totalDailyCoins || 0;

            if (newValue > oldValue) {
                // 코인 증가 애니메이션
                coinNumberElement.style.transform = 'scale(1.2)';
                coinNumberElement.style.color = '#ffa940';
                setTimeout(() => {
                    coinNumberElement.style.transform = 'scale(1)';
                    coinNumberElement.style.color = '';
                }, 300);
            }

            coinNumberElement.textContent = newValue;
        }
    }

    // 코인 애니메이션 표시
    showCoinAnimation(amount) {
        const coinDisplay = document.querySelector('.coin-display');
        if (!coinDisplay) return;

        const animation = document.createElement('div');
        animation.className = 'coin-animation';
        animation.textContent = amount;
        animation.style.cssText = `
            position: absolute;
            top: -20px;
            right: 0;
            background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
            color: #333;
            padding: 8px 12px;
            border-radius: 20px;
            font-weight: bold;
            font-size: 16px;
            box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
            animation: coinFloat 1.5s ease-out forwards;
            z-index: 1000;
            border: 2px solid #ffa500;
        `;

        // CSS 애니메이션 추가
        if (!document.querySelector('#coin-animation-style')) {
            const style = document.createElement('style');
            style.id = 'coin-animation-style';
            style.textContent = `
                @keyframes coinFloat {
                    0% {
                        transform: translateY(0) scale(0.8);
                        opacity: 0;
                    }
                    20% {
                        transform: translateY(-10px) scale(1.2);
                        opacity: 1;
                    }
                    80% {
                        transform: translateY(-30px) scale(1);
                        opacity: 1;
                    }
                    100% {
                        transform: translateY(-50px) scale(0.8);
                        opacity: 0;
                    }
                }
            `;
            document.head.appendChild(style);
        }

        coinDisplay.style.position = 'relative';
        coinDisplay.appendChild(animation);

        // 애니메이션 완료 후 제거
        setTimeout(() => {
            if (animation.parentNode) {
                animation.parentNode.removeChild(animation);
            }
        }, 1500);
    }

    // 음성 재생 메서드들
    async playWordAudio(card) {
        const wordText = card.querySelector('.word-english')?.textContent;
        const wordId = card.getAttribute('data-word-id');
        const audioFileName = card.getAttribute('data-audio-file'); // 추가: audio_file 속성

        console.log('🔊 단어 오디오 재생 시작:', {
            wordText,
            wordId,
            audioFileName
        });

        if (!wordText) return;

        try {
            const soundBtn = card.querySelector('.word-sound');
            if (soundBtn) {
                soundBtn.style.color = '#007bff';
                soundBtn.style.transform = 'scale(1.2)';
            }

            // 업로드된 오디오 파일이 있는지 확인
            let audioUrl = null;
            if (audioFileName) {
                console.log('📁 오디오 파일명 확인:', audioFileName);
                // S3 URL인지 확인 (amazonaws.com 포함)
                if (audioFileName.includes('amazonaws.com')) {
                    audioUrl = audioFileName; // S3 URL 그대로 사용
                    console.log('☁️ S3 URL 사용:', audioUrl);
                } else {
                    // 로컬 파일 경로인 경우
                    audioUrl = audioFileName.startsWith('/audio/words/')
                        ? audioFileName
                        : `/audio/words/${audioFileName}`;
                    console.log('📂 로컬 파일 경로 사용:', audioUrl);
                }
            } else {
                console.log('⚠️ 오디오 파일명이 없습니다. TTS를 사용합니다.');
            }
            if (audioUrl) {
                return new Promise((resolve, reject) => {
                    const audio = new Audio(audioUrl);
                    this.currentPlaybook = audio;
                    
                    audio.onended = () => {
                        if (soundBtn) {
                            soundBtn.style.color = '';
                            soundBtn.style.transform = 'scale(1)';
                        }
                        this.logAudioPlay('word', wordId);
                        this.currentPlayback = null;
                        resolve();
                    };
                    
                    audio.onerror = async (error) => {
                        console.warn(`🎵 오디오 파일 재생 실패 (${audioUrl}):`, error);
                        console.log('📢 TTS로 폴백 재생');
                        
                        // TTS로 폴백
                        try {
                            await this.speakText(wordText);
                        } catch (ttsError) {
                            console.error('TTS도 실패:', ttsError);
                        }
                        
                        if (soundBtn) {
                            soundBtn.style.color = '';
                            soundBtn.style.transform = 'scale(1)';
                        }
                        this.logAudioPlay('word', wordId);
                        resolve(); // 에러가 아닌 정상 종료로 처리
                    };
                    
                    // 재생 시작
                    audio.play().catch(async (playError) => {
                        console.warn(`🎵 오디오 재생 시작 실패 (${audioUrl}):`, playError);
                        console.log('📢 TTS로 폴백 재생');
                        
                        // TTS로 폴백
                        try {
                            await this.speakText(wordText);
                        } catch (ttsError) {
                            console.error('TTS도 실패:', ttsError);
                        }
                        
                        if (soundBtn) {
                            soundBtn.style.color = '';
                            soundBtn.style.transform = 'scale(1)';
                        }
                        this.logAudioPlay('word', wordId);
                        resolve(); // 에러가 아닌 정상 종료로 처리
                    });
                });
            }
            // 오디오 파일이 없으면 TTS 사용
            return new Promise(async (resolve) => {
                await this.speakText(wordText);
                if (soundBtn) {
                    soundBtn.style.color = '';
                    soundBtn.style.transform = 'scale(1)';
                }
            if (wordId) {
                    this.logAudioPlay('word', wordId);
            }
                resolve();
            });
        } catch (error) {
            console.error('음성 재생 실패:', error);
        }
    }

    async playSentenceAudio(card) {
        const sentenceText = card.querySelector('.sentence-text')?.textContent;
        const sentenceId = card.getAttribute('data-sentence-id');
        const audioFileName = card.getAttribute('data-audio-file'); // 추가: audio_file 속성

        if (!sentenceText) return;

        try {
            const soundBtn = card.querySelector('.sentence-sound');
            if (soundBtn) {
                soundBtn.style.color = '#007bff';
                soundBtn.style.transform = 'scale(1.2)';
            }

            // 업로드된 오디오 파일이 있는지 확인
            let audioUrl = null;
            if (audioFileName) {
                console.log('📁 문장 오디오 파일명 확인:', audioFileName);
                // S3 URL인지 확인 (amazonaws.com 포함)
                if (audioFileName.includes('amazonaws.com')) {
                    audioUrl = audioFileName; // S3 URL 그대로 사용
                    console.log('☁️ S3 URL 사용:', audioUrl);
                } else {
                    // 로컬 파일 경로인 경우
                    audioUrl = audioFileName.startsWith('/audio/sentences/')
                        ? audioFileName
                        : `/audio/sentences/${audioFileName}`;
                    console.log('📂 로컬 파일 경로 사용:', audioUrl);
                }
            } else {
                console.log('⚠️ 문장 오디오 파일명이 없습니다. TTS를 사용합니다.');
            }
            if (audioUrl) {
                return new Promise((resolve, reject) => {
                    const audio = new Audio(audioUrl);
                    this.currentPlayback = audio;
                    
                    audio.onended = () => {
                        if (soundBtn) {
                            soundBtn.style.color = '';
                            soundBtn.style.transform = 'scale(1)';
                        }
                        this.logAudioPlay('sentence', sentenceId);
                        this.currentPlayback = null;
                        resolve();
                    };
                    
                    audio.onerror = async (error) => {
                        console.warn(`🎵 문장 오디오 파일 재생 실패 (${audioUrl}):`, error);
                        console.log('📢 TTS로 폴백 재생');
                        
                        // TTS로 폴백
                        try {
                            await this.speakText(sentenceText);
                        } catch (ttsError) {
                            console.error('TTS도 실패:', ttsError);
                        }
                        
                        if (soundBtn) {
                            soundBtn.style.color = '';
                            soundBtn.style.transform = 'scale(1)';
                        }
                        this.logAudioPlay('sentence', sentenceId);
                        resolve(); // 에러가 아닌 정상 종료로 처리
                    };
                    
                    // 재생 시작
                    audio.play().catch(async (playError) => {
                        console.warn(`🎵 문장 오디오 재생 시작 실패 (${audioUrl}):`, playError);
                        console.log('📢 TTS로 폴백 재생');
                        
                        // TTS로 폴백
                        try {
                            await this.speakText(sentenceText);
                        } catch (ttsError) {
                            console.error('TTS도 실패:', ttsError);
                        }
                        
                        if (soundBtn) {
                            soundBtn.style.color = '';
                            soundBtn.style.transform = 'scale(1)';
                        }
                        this.logAudioPlay('sentence', sentenceId);
                        resolve(); // 에러가 아닌 정상 종료로 처리
                    });
                });
            }
            // 오디오 파일이 없으면 TTS 사용
            return new Promise(async (resolve) => {
                await this.speakText(sentenceText);
                if (soundBtn) {
                    soundBtn.style.color = '';
                    soundBtn.style.transform = 'scale(1)';
                }
            if (sentenceId) {
                    this.logAudioPlay('sentence', sentenceId);
            }
                resolve();
            });
        } catch (error) {
            console.error('음성 재생 실패:', error);
        }
    }

    // 즐겨찾기 토글
    async toggleWordFavorite(card) {
        const wordId = card.getAttribute('data-word-id');
        if (!wordId) return;

        try {
            const response = await fetch(`/learning/api/words/${wordId}/favorite`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const result = await response.json();
                const favoriteBtn = card.querySelector('.word-favorite');

                if (favoriteBtn) {
                    favoriteBtn.textContent = result.isFavorite ? '❤️' : '♡';
                    favoriteBtn.style.color = result.isFavorite ? '#ff6b6b' : '';

                    favoriteBtn.style.transform = 'scale(1.3)';
                    setTimeout(() => {
                        favoriteBtn.style.transform = 'scale(1)';
                    }, 200);
                }
            }

        } catch (error) {
            console.error('즐겨찾기 업데이트 실패:', error);
        }
    }

    // 학습 시작 메서드들
    startWordsLearning() {
        this.isPlaying = true;
        /*this.showMessage('단어 학습을 시작합니다! 📚');*/
        this.playAllWords();
    }

    startSentencesLearning() {
        this.isPlaying = true;
        /*this.showMessage('문장 학습을 시작합니다! 📝');*/
        this.playAllSentences();
    }

    // 학습 중지 메서드
    stopLearning() {
        this.isPlaying = false;

        // 현재 재생 중인 오디오 중지
        if (this.currentPlayback) {
            this.currentPlayback.pause();
            this.currentPlayback = null;
        }

        // 음성 합성 중지
        if ('speechSynthesis' in window) {
            speechSynthesis.cancel();
        }

        // 모든 카드 스타일 초기화
        const allCards = document.querySelectorAll('.word-card, .sentence-card');
        allCards.forEach(card => {
            card.style.background = '';
            card.style.borderColor = '';
        });

        /*this.showMessage('학습이 중지되었습니다! ⏹️');*/
    }

    // 모든 단어 순차 재생
    async playAllWords() {
        const wordCards = document.querySelectorAll('.word-card');

        for (let i = 0; i < wordCards.length && this.isPlaying; i++) {
            const card = wordCards[i];

            card.style.background = 'linear-gradient(135deg, #fff3e0 0%, #ffcc02 100%)';
            card.style.borderColor = '#ff9800';

            // 음성 재생이 완전히 끝날 때까지 대기
            await this.playWordAudio(card);

            // 코인 지급도 반드시 호출
            const wordText = card.querySelector('.word-english')?.textContent;
            if (wordText) {
                await this.addCoinAfterAudio('word', wordText);
            }
            // 재생 중지 확인
            if (!this.isPlaying) break;

            card.style.background = '';
            card.style.borderColor = '';
        }

        if (this.isPlaying) {
        /*this.showMessage('모든 단어 학습 완료! 🎉');*/
            this.isPlaying = false;
            // 버튼 상태 복원
            const startButtons = document.querySelectorAll('.start-button');
            startButtons.forEach(btn => {
                btn.textContent = '🚀 Start';
                btn.style.backgroundColor = '#007bff';
            });
        }
    }

    // 모든 문장 순차 재생
    async playAllSentences() {
        const sentenceCards = document.querySelectorAll('.sentence-card');

        for (let i = 0; i < sentenceCards.length && this.isPlaying; i++) {
            const card = sentenceCards[i];

            card.style.background = 'linear-gradient(135deg, #fff3e0 0%, #ffcc02 100%)';
            card.style.borderColor = '#ff9800';

            // 음성 재생이 완전히 끝날 때까지 대기
            await this.playSentenceAudio(card);

            // 코인 지급도 반드시 호출
            const sentenceText = card.querySelector('.sentence-text')?.textContent;
            if (sentenceText) {
                await this.addCoinAfterAudio('sentence', sentenceText.substring(0, 20) + '...');
            }
            // 재생 중지 확인
            if (!this.isPlaying) break;

            card.style.background = '';
            card.style.borderColor = '';
        }

        if (this.isPlaying) {
        /*this.showMessage('모든 문장 학습 완료! 🎉');*/
            this.isPlaying = false;
            // 버튼 상태 복원
            const startButtons = document.querySelectorAll('.start-button');
            startButtons.forEach(btn => {
                btn.textContent = '🚀 Start';
                btn.style.backgroundColor = '#007bff';
            });
        }
    }

    // 백엔드 API 호출 메서드들
    async updateWordProgress(wordId, isCompleted) {
        try {
            const response = await fetch('/learning/api/progress/word', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ wordId, isCompleted })
            });

            if (!response.ok) throw new Error('진행상황 업데이트 실패');

            return await response.json();

        } catch (error) {
            console.error('단어 진행상황 업데이트 실패:', error);
            return null;
        }
    }

    async updateSentenceProgress(sentenceId, isCompleted) {
        try {
            const response = await fetch('/learning/api/progress/sentence', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ sentenceId, isCompleted })
            });

            if (!response.ok) throw new Error('진행상황 업데이트 실패');

            return await response.json();

        } catch (error) {
            console.error('문장 진행상황 업데이트 실패:', error);
            return null;
        }
    }

    async logAudioPlay(type, itemId) {
        try {
            const response = await fetch(`/learning/api/audio/play`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ type, itemId })
            });
        } catch (error) {
            console.error('음성 재생 로그 실패:', error);
        }
    }

    // 통계 새로고침 및 UI 업데이트
    async refreshStats() {
        try {
            this.stats = await this.fetchStats();
            this.updateUI();
        } catch (error) {
            console.error('통계 새로고침 실패:', error);
        }
    }

    // UI 업데이트
    updateUI() {
        const {
            completedWords = 0,
            totalWords = 1,
            completedSentences = 0,
            totalSentences = 0,
            coinsEarned = 0
        } = this.stats;

        // 헤더 정보 업데이트
        this.updateElement('.header-left p', `오늘 학습: 단어 ${this.completedWords.size}개, 문장 ${this.completedSentences.size}개`);

        // 섹션 부제목 업데이트
        this.updateElement('.section-card:first-child .section-subtitle',
            `오늘의 단어 ${this.words.length}개를 학습해보세요! (${this.completedWords.size}/${this.words.length})`);
        this.updateElement('.section-card:last-child .section-subtitle',
            `오늘의 문장 ${this.sentences.length}개를 학습해보세요! (${this.completedSentences.size}/${this.sentences.length})`);

        // 진행률 계산
        const wordProgress = this.words.length > 0 ? (this.completedWords.size / this.words.length) * 100 : 0;
        const sentenceProgress = this.sentences.length > 0 ? (this.completedSentences.size / this.sentences.length) * 100 : 0;

        console.log(`📊 진행률 - 단어: ${wordProgress.toFixed(1)}%, 문장: ${sentenceProgress.toFixed(1)}%`);
    }

    // 유틸리티 메서드들
    updateElement(selector, content) {
        const element = document.querySelector(selector);
        if (element) {
            element.textContent = content;
        }
    }

    speakText(text) {
        return new Promise((resolve) => {
        if ('speechSynthesis' in window) {
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.lang = 'en-US';
            utterance.rate = 0.8;

                utterance.onend = () => {
                    resolve();
                };

                utterance.onerror = () => {
                    resolve();
                };

                utterance.onpause = () => {
                    resolve();
                };

                utterance.oncancel = () => {
                    resolve();
                };

            speechSynthesis.speak(utterance);
            } else {
                resolve();
        }
        });
    }

    showMessage(message) {
        if (window.kiribocaApp && window.kiribocaApp.showToast) {
            window.kiribocaApp.showToast('학습', message);
        } else {
            console.log('📢', message);
        }
    }

    showError(message) {
        console.error('❌', message);
        this.showMessage(message);
    }

    // 정리 메서드
    cleanup() {
        try {
            // 재생 중지
            this.stopLearning();

            // 이벤트 리스너 플래그 초기화
            this.eventListenersAdded = false;
            this.isInitialized = false;

            // MutationObserver 정리
            if (this.observer) {
                this.observer.disconnect();
            }

            console.log('🧹 향상된 통합 학습 관리자 정리 완료');
        } catch (error) {
            console.error('❌ 정리 중 오류:', error);
        }
    }

    // sidebar 연동 필터 이벤트 추가 (정적 옵션 사용)
    setupSidebarFilters() {
        const levelSelect = document.getElementById('level-select');
        const daySelect = document.getElementById('day-select');

        if (levelSelect) {
            // 기존 이벤트 리스너 제거 (중복 방지)
            levelSelect.removeEventListener('change', this.handleLevelChange);
            levelSelect.addEventListener('change', this.handleLevelChange.bind(this));
        }

        if (daySelect) {
            // 기존 이벤트 리스너 제거 (중복 방지)
            daySelect.removeEventListener('change', this.handleDayChange);
            daySelect.addEventListener('change', this.handleDayChange.bind(this));
        }

        console.log('🔗 사이드바 필터 이벤트 설정 완료 (정적 옵션 사용)');
    }

    // 레벨 변경 핸들러
    async handleLevelChange(event) {
        const newLevel = parseInt(event.target.value);
        if (newLevel !== this.currentLevel && !isNaN(newLevel)) {
            console.log(`📊 레벨 변경: ${this.currentLevel} → ${newLevel}`);
            this.currentLevel = newLevel;
            this.currentDay = 1; // 레벨 변경 시 Day 1로 리셋

            // Day 선택 박스도 업데이트
            const daySelect = document.getElementById('day-select');
            if (daySelect) {
                daySelect.value = 1;
            }

            // 데이터 재로드
            await this.loadLearningData();
            this.updateHeader();
        }
    }

    // Day 변경 핸들러
    async handleDayChange(event) {
        const newDay = parseInt(event.target.value);
        if (newDay !== this.currentDay && !isNaN(newDay)) {
            console.log(`📅 Day 변경: ${this.currentDay} → ${newDay}`);
            this.currentDay = newDay;

            // 데이터 재로드
            await this.loadLearningData();
            this.updateHeader();
        }
    }

    updateHeader() {
        // 헤더의 Level/Day 텍스트 동적 변경
        const headerTitle = document.querySelector('.header-left h1');
        if (headerTitle) {
            headerTitle.textContent = `Level ${this.currentLevel} - Day ${this.currentDay}`;
        }
    }
}

// 전역에서 사용할 수 있도록 설정
window.EnhancedIntegratedLearningManager = EnhancedIntegratedLearningManager;

// 초기화 함수 - 개선된 버전
function initEnhancedIntegratedLearningManager() {
    // 기존 인스턴스 정리
    if (window.enhancedIntegratedLearningManager) {
        try {
            // 기존 이벤트 리스너들 정리
            if (window.enhancedIntegratedLearningManager.cleanup) {
                window.enhancedIntegratedLearningManager.cleanup();
            }
        } catch (e) {
            console.log('기존 인스턴스 정리 중 오류:', e);
        }
        window.enhancedIntegratedLearningManager = null;
    }

    // 잠시 대기 후 새로운 인스턴스 생성
    setTimeout(() => {
        try {
            window.enhancedIntegratedLearningManager = new EnhancedIntegratedLearningManager();
            console.log('✅ 향상된 통합 학습 관리자 초기화 완료');
        } catch (error) {
            console.error('❌ 향상된 통합 학습 관리자 초기화 실패:', error);
        }
    }, 100);
}

// 전역 로딩 스피너 표시 함수 추가
function showLoadingSpinner() {
    let spinner = document.getElementById('loading-spinner');
    if (!spinner) {
        spinner = document.createElement('div');
        spinner.id = 'loading-spinner';
        spinner.style.position = 'fixed';
        spinner.style.top = '0';
        spinner.style.left = '0';
        spinner.style.width = '100vw';
        spinner.style.height = '100vh';
        spinner.style.background = 'rgba(255,255,255,0.7)';
        spinner.style.display = 'flex';
        spinner.style.alignItems = 'center';
        spinner.style.justifyContent = 'center';
        spinner.style.zIndex = '9999';
        spinner.innerHTML = '<div style="font-size:2rem; color:#34E5C2;">로딩 중...</div>';
        document.body.appendChild(spinner);
    }
    spinner.style.display = 'flex';
}
function hideLoadingSpinner() {
    const spinner = document.getElementById('loading-spinner');
    if (spinner) spinner.style.display = 'none';
}
// DOMContentLoaded에서 안전하게 실행
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        showLoadingSpinner();
        initEnhancedIntegratedLearningManager();
    });
} else {
    showLoadingSpinner();
    initEnhancedIntegratedLearningManager();
}
// EnhancedIntegratedLearningManager 내에서 데이터 렌더링 후 hideLoadingSpinner() 호출
// renderWordsToHTML, renderSentencesToHTML 마지막에 hideLoadingSpinner() 추가