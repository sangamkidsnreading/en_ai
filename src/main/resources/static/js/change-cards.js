// 카드 변경 관리자 (EnhancedIntegratedLearningManager 중복 제거)
class CardChangeManager {
    constructor() {
        this.currentLevel = 1;
        this.currentDay = 1;
        this.isLoading = false;
        this.init();
    }

    async init() {
        console.log('🔄 카드 변경 관리자 초기화');
        console.log('🔄 카드 변경 관리자 초기화 시작');

        // 이벤트 리스너 설정
        this.setupEventListeners();

        // 초기 카드 로드
        await this.loadCards();

        console.log('✅ 카드 변경 관리자 초기화 완료');
    }

    setupEventListeners() {
        // 레벨 선택 이벤트
        const levelSelect = document.getElementById('level-select');
        if (levelSelect) {
            levelSelect.addEventListener('change', async (e) => {
                const newLevel = parseInt(e.target.value);
                if (newLevel !== this.currentLevel && !isNaN(newLevel)) {
                    this.currentLevel = newLevel;
                    this.currentDay = 1; // 레벨 변경 시 Day 1로 리셋

                    // Day 선택 박스도 업데이트
                    const daySelect = document.getElementById('day-select');
                    if (daySelect) {
                        daySelect.value = 1;
                    }

                    await this.updateDayOptions();
                    await this.loadCards();
                }
            });
        }

        // Day 선택 이벤트
        const daySelect = document.getElementById('day-select');
        if (daySelect) {
            daySelect.addEventListener('change', async (e) => {
                const newDay = parseInt(e.target.value);
                if (newDay !== this.currentDay && !isNaN(newDay)) {
                    this.currentDay = newDay;
                    await this.loadCards();
                }
            });
        }

        console.log('🔗 카드 변경 이벤트 리스너 설정 완료');
    }

    async updateDayOptions() {
        try {
            const response = await fetch(`/api/sidebar/days?level=${this.currentLevel}`);
            if (!response.ok) throw new Error('Day 옵션 로드 실패');

            const availableDays = await response.json();
            const daySelect = document.getElementById('day-select');

            if (daySelect && availableDays.length > 0) {
                // 기존 옵션 제거 (첫 번째 "모든 Day" 옵션 제외)
                while (daySelect.children.length > 1) {
                    daySelect.removeChild(daySelect.lastChild);
                }

                // 새로운 Day 옵션 추가
                availableDays.forEach(day => {
                    const option = document.createElement('option');
                    option.value = day;
                    option.textContent = `Day ${day}`;
                    if (day === this.currentDay) {
                        option.selected = true;
                    }
                    daySelect.appendChild(option);
                });
            }
        } catch (error) {
            console.error('Day 옵션 업데이트 실패:', error);
        }
    }

    async loadCards() {
        if (this.isLoading) return;

        this.isLoading = true;

        try {
            console.log(`🔄 카드 로드 중 - Level: ${this.currentLevel}, Day: ${this.currentDay}`);

            const response = await fetch(`/api/sidebar/filter?level=${this.currentLevel}&day=${this.currentDay}`);
            if (!response.ok) throw new Error('카드 로드 실패');

            const data = await response.json();

            // 헤더 정보 업데이트
            this.updateHeader(data);

            // 향상된 통합 학습 관리자가 있으면 그것을 우선 사용
            if (window.enhancedIntegratedLearningManager) {
                console.log('🔄 향상된 통합 학습 관리자에게 데이터 전달');
                window.enhancedIntegratedLearningManager.words = data.words || [];
                window.enhancedIntegratedLearningManager.sentences = data.sentences || [];
                window.enhancedIntegratedLearningManager.currentLevel = this.currentLevel;
                window.enhancedIntegratedLearningManager.currentDay = this.currentDay;
                
                // 카드 재렌더링
                window.enhancedIntegratedLearningManager.renderWordsToHTML();
                window.enhancedIntegratedLearningManager.renderSentencesToHTML();
                window.enhancedIntegratedLearningManager.updateUI();
                
                // 이벤트 재설정 (새로운 카드가 로드되었으므로 리셋)
                window.enhancedIntegratedLearningManager.eventListenersAdded = false;
                window.enhancedIntegratedLearningManager.setupEvents();
            } else {
                // 기존 방식 (fallback)
            this.renderWords(data.words || []);
            this.renderSentences(data.sentences || []);

            if (window.integratedLearningManager) {
                window.integratedLearningManager.words = data.words || [];
                window.integratedLearningManager.sentences = data.sentences || [];
                window.integratedLearningManager.currentLevel = this.currentLevel;
                window.integratedLearningManager.currentDay = this.currentDay;
                window.integratedLearningManager.updateUI();
                }
            }

        } catch (error) {
            console.error('❌ 카드 로드 실패:', error);
            this.showError('카드를 불러오는데 실패했습니다.');
        } finally {
            this.isLoading = false;
        }
    }

    updateHeader(data) {
        // 레벨과 Day 정보 업데이트
        const headerTitle = document.querySelector('.header-left h1');
        if (headerTitle) {
            headerTitle.textContent = `Level ${this.currentLevel} - Day ${this.currentDay}`;
        }

        // 학습 정보 업데이트
        const headerSubtitle = document.querySelector('.header-left p');
        if (headerSubtitle) {
            const totalWords = data.totalWords || 0;
            const totalSentences = data.totalSentences || 0;
            headerSubtitle.textContent = `오늘 학습: 단어 ${totalWords}개, 문장 ${totalSentences}개`;
        }

        // 섹션 부제목 업데이트
        const wordSubtitle = document.querySelector('.section-card:first-child .section-subtitle');
        if (wordSubtitle) {
            const totalWords = data.totalWords || 0;
            wordSubtitle.textContent = `오늘의 단어 ${totalWords}개를 학습해보세요! (0/${totalWords})`;
        }

        const sentenceSubtitle = document.querySelector('.section-card:last-child .section-subtitle');
        if (sentenceSubtitle) {
            const totalSentences = data.totalSentences || 0;
            sentenceSubtitle.textContent = `오늘의 문장 ${totalSentences}개를 학습해보세요! (0/${totalSentences})`;
        }
    }

    renderWords(words) {
        // 향상된 통합 학습 관리자가 있으면 이 메서드는 사용하지 않음
        if (window.enhancedIntegratedLearningManager) {
            console.log('🔄 향상된 통합 학습 관리자가 있으므로 기본 렌더링 스킵');
            return;
        }

        const wordsGrid = document.querySelector('.words-grid');
        if (!wordsGrid) return;

        wordsGrid.innerHTML = '';

        if (words.length === 0) {
            wordsGrid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">
                    <div style="font-size: 48px; margin-bottom: 16px;">📝</div>
                    <div style="font-size: 18px; margin-bottom: 8px;">이 레벨/Day에는 단어가 없습니다</div>
                    <div style="font-size: 14px;">다른 레벨이나 Day를 선택해보세요!</div>
                </div>
            `;
            return;
        }

        words.forEach(word => {
            const wordCard = document.createElement('div');
            wordCard.className = 'word-card';
            wordCard.setAttribute('data-word-id', word.id);

            wordCard.innerHTML = `
                <div class="word-favorite">♡</div>
                <div class="word-english">${word.text || 'Word'}</div>
                <div class="word-korean">${word.meaning || '의미'}</div>
                <div class="word-sound">🔊</div>
            `;

            wordsGrid.appendChild(wordCard);
        });
    }

    renderSentences(sentences) {
        // 향상된 통합 학습 관리자가 있으면 이 메서드는 사용하지 않음
        if (window.enhancedIntegratedLearningManager) {
            console.log('🔄 향상된 통합 학습 관리자가 있으므로 기본 렌더링 스킵');
            return;
        }

        const sentencesGrid = document.querySelector('.sentences-grid');
        if (!sentencesGrid) return;

        sentencesGrid.innerHTML = '';

        if (sentences.length === 0) {
            sentencesGrid.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #666;">
                    <div style="font-size: 48px; margin-bottom: 16px;">📝</div>
                    <div style="font-size: 18px; margin-bottom: 8px;">이 레벨/Day에는 문장이 없습니다</div>
                    <div style="font-size: 14px;">다른 레벨이나 Day를 선택해보세요!</div>
                </div>
            `;
            return;
        }

        sentences.forEach(sentence => {
            const sentenceCard = document.createElement('div');
            sentenceCard.className = 'sentence-card';
            sentenceCard.setAttribute('data-sentence-id', sentence.id);

            const englishText = sentence.english || sentence.text || 'Sample sentence';
            const koreanText = sentence.korean || sentence.meaning || sentence.translation || '';

            sentenceCard.innerHTML = `
                <div class="sentence-content">
                    <div class="sentence-text">${englishText}</div>
                    ${koreanText ? `<div class="sentence-korean">${koreanText}</div>` : ''}
                </div>
                <div class="sentence-sound">🔊</div>
            `;

            sentencesGrid.appendChild(sentenceCard);
        });
    }

    showError(message) {
        console.error('❌', message);
        if (window.kiribocaApp && window.kiribocaApp.showToast) {
            window.kiribocaApp.showToast('오류', message);
        }
    }
}

// 전역 인스턴스
window.cardChangeManager = null;

// 초기화 함수
function initCardChangeManager() {
    if (!window.cardChangeManager) {
        window.cardChangeManager = new CardChangeManager();
    }
}

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', () => {
    // 학습 페이지에서만 초기화
    if (document.getElementById('learning-page')) {
        setTimeout(initCardChangeManager, 1000);
    }
});