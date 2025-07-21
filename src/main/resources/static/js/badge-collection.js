class BadgeCollection {
    constructor() {
        this.userBadges = [];
        this.allBadges = [];
        this.userStats = null; // 추가: 사용자 뱃지 진행도 저장
        this.init();
    }

    async init() {
        await this.loadUserBadges();
        await this.loadAllBadges();
        await this.loadUserStats();
        this.renderBadgeCollection();
    }

    /**
     * 사용자가 획득한 뱃지 로드
     */
    async loadUserBadges() {
        try {
            console.log('🔍 사용자 뱃지 로드 시작...');
            const response = await fetch('/api/user-badges/my-badges');
            console.log('📡 API 응답 상태:', response.status);
            
            if (response.ok) {
                this.userBadges = await response.json();
                console.log('✅ 사용자 뱃지 로드 완료:', this.userBadges.length, '개');
                console.log('📋 뱃지 상세:', this.userBadges);
            } else {
                const errorText = await response.text();
                console.error('❌ 사용자 뱃지 로드 실패:', response.status, errorText);
                this.userBadges = [];
            }
        } catch (error) {
            console.error('❌ 사용자 뱃지 로드 오류:', error);
            this.userBadges = [];
        }
    }

    /**
     * 모든 뱃지 설정 로드
     */
    async loadAllBadges() {
        try {
            // 대시보드용 모든 유저 접근 가능 API 사용
            const response = await fetch('/api/dashboard/badges');
            if (response.ok) {
                const data = await response.json();
                this.allBadges = data.badges || [];
            } else {
                console.error('뱃지 설정 로드 실패:', response.status);
                this.allBadges = [];
            }
        } catch (error) {
            console.error('뱃지 설정 로드 오류:', error);
            this.allBadges = [];
        }
    }

    /**
     * 내 뱃지 진행도(통계) 로드
     */
    async loadUserStats() {
        try {
            const response = await fetch('/api/user-badges/my-stats');
            if (response.ok) {
                this.userStats = await response.json();
            } else {
                this.userStats = null;
            }
        } catch (e) {
            this.userStats = null;
        }
    }

    /**
     * 뱃지 조건 텍스트
     */
    getBadgeConditionText(badge) {
        if (badge.attendanceCount) return `출석 ${badge.attendanceCount}회`;
        if (badge.streakCount) return `연속 출석 ${badge.streakCount}일`;
        if (badge.wordsCount) return `단어 ${badge.wordsCount}개 학습`;
        if (badge.sentencesCount) return `문장 ${badge.sentencesCount}개 학습`;
        if (badge.wordReviewCount) return `단어 복습 ${badge.wordReviewCount}회`;
        if (badge.sentenceReviewCount) return `문장 복습 ${badge.sentenceReviewCount}회`;
        return '';
    }
    getBadgeProgressText(badge) {
        if (!this.userStats) return '';
        if (badge.attendanceCount)
            return `현재: ${this.userStats.attendanceCount || 0} / ${badge.attendanceCount}회`;
        if (badge.streakCount)
            return `현재: ${this.userStats.streakCount || 0} / ${badge.streakCount}일`;
        if (badge.wordsCount)
            return `현재: ${this.userStats.completedWordsCount || 0} / ${badge.wordsCount}개`;
        if (badge.sentencesCount)
            return `현재: ${this.userStats.completedSentencesCount || 0} / ${badge.sentencesCount}개`;
        if (badge.wordReviewCount)
            return `현재: ${this.userStats.wordReviewCount || 0} / ${badge.wordReviewCount}회`;
        if (badge.sentenceReviewCount)
            return `현재: ${this.userStats.sentenceReviewCount || 0} / ${badge.sentenceReviewCount}회`;
        return '';
    }

    /**
     * 뱃지 컬렉션 렌더링
     */
    renderBadgeCollection() {
        const gridContainer = document.getElementById('badge-collection-grid');
        const emptyState = document.getElementById('badge-empty-state');
        const totalCount = document.getElementById('badge-total-count');

        if (!gridContainer) return;

        // 획득한 뱃지 개수 업데이트
        const earnedCount = this.userBadges.length;
        if (totalCount) {
            totalCount.textContent = earnedCount;
        }

        // 획득한 뱃지가 없으면 빈 상태 표시
        if (earnedCount === 0) {
            gridContainer.innerHTML = '';
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            return;
        }

        // 빈 상태 숨기기
        if (emptyState) {
            emptyState.style.display = 'none';
        }

        // 뱃지 그리드 생성
        const badgeHTML = this.allBadges.map(badge => {
            const userBadge = this.userBadges.find(ub => ub.badgeId === badge.id);
            const isEarned = !!userBadge;
            return this.createBadgeCard(badge, userBadge, isEarned);
        }).join('');

        gridContainer.innerHTML = badgeHTML;
    }

    /**
     * 뱃지 카드(앞/뒷면) 생성
     */
    createBadgeCard(badge, userBadge, isEarned) {
        const earnedClass = isEarned ? 'earned' : 'locked';
        const earnedDate = userBadge ? this.formatDate(userBadge.earnedAt) : '';
        const condition = this.getBadgeConditionText(badge);
        const progress = this.getBadgeProgressText(badge);
        // 조건 설명 텍스트
        let howToEarn = '';
        if (badge.attendanceCount) howToEarn = `출석을 ${badge.attendanceCount}회 하면 획득할 수 있습니다.`;
        else if (badge.streakCount) howToEarn = `연속 출석을 ${badge.streakCount}일 달성하면 획득할 수 있습니다.`;
        else if (badge.wordsCount) howToEarn = `단어를 ${badge.wordsCount}개 학습하면 획득할 수 있습니다.`;
        else if (badge.sentencesCount) howToEarn = `문장을 ${badge.sentencesCount}개 학습하면 획득할 수 있습니다.`;
        else if (badge.wordReviewCount) howToEarn = `단어 복습을 ${badge.wordReviewCount}회 하면 획득할 수 있습니다.`;
        else if (badge.sentenceReviewCount) howToEarn = `문장 복습을 ${badge.sentenceReviewCount}회 하면 획득할 수 있습니다.`;
        return `
        <div class="badge-card ${earnedClass}" data-badge-id="${badge.id}">
            <div class="front">
                <div class="badge-icon">${badge.icon}</div>
                <div class="badge-name">${badge.name}</div>
            </div>
            <div class="back">
                <div class="badge-description">${badge.description || ''}</div>
                <div class="badge-condition">${condition}</div>
                <div class="badge-howto">${howToEarn}</div>
                <div class="badge-progress">${progress}</div>
                ${isEarned ? `<div class="badge-earned-date">${earnedDate} 획득</div>` : ''}
            </div>
        </div>
        `;
    }

    /**
     * 개별 뱃지 아이템 생성
     */
    createBadgeItem(badge, userBadge, isEarned) {
        const earnedClass = isEarned ? 'earned' : 'locked';
        const earnedDate = userBadge ? this.formatDate(userBadge.earnedAt) : '';
        
        return `
            <div class="badge-item ${earnedClass}" data-badge-id="${badge.id}">
                <div class="badge-icon">${badge.icon}</div>
                <div class="badge-name">${badge.name}</div>
                <div class="badge-description">${badge.description}</div>
                ${isEarned ? `<div class="badge-earned-date">${earnedDate} 획득</div>` : ''}
            </div>
        `;
    }

    /**
     * 날짜 포맷팅
     */
    formatDate(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffTime = Math.abs(now - date);
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 1) {
            return '오늘';
        } else if (diffDays <= 7) {
            return `${diffDays}일 전`;
        } else {
            return date.toLocaleDateString('ko-KR', {
                month: 'short',
                day: 'numeric'
            });
        }
    }

    /**
     * 뱃지 획득 처리
     */
    async earnBadge(badgeId) {
        try {
            const response = await fetch(`/api/user-badges/earn/${badgeId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const earnedBadge = await response.json();
                console.log('뱃지 획득:', earnedBadge);
                
                // 뱃지 컬렉션 새로고침
                await this.loadUserBadges();
                this.renderBadgeCollection();
                
                // 획득 알림 표시
                this.showEarnedNotification(earnedBadge);
                
                return earnedBadge;
            } else {
                console.error('뱃지 획득 실패:', response.status);
                return null;
            }
        } catch (error) {
            console.error('뱃지 획득 오류:', error);
            return null;
        }
    }

    /**
     * 뱃지 획득 알림 표시
     */
    showEarnedNotification(badge) {
        // 기존 알림 제거
        const existingNotification = document.querySelector('.badge-notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        // 새 알림 생성
        const notification = document.createElement('div');
        notification.className = 'badge-notification';
        notification.innerHTML = `
            <div class="notification-content">
                <div class="notification-icon">${badge.icon}</div>
                <div class="notification-text">
                    <div class="notification-title">새로운 뱃지 획득!</div>
                    <div class="notification-badge">${badge.name}</div>
                </div>
            </div>
        `;

        // 스타일 적용
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 16px 20px;
            border-radius: 12px;
            box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
            z-index: 1000;
            transform: translateX(100%);
            transition: transform 0.3s ease;
            max-width: 300px;
        `;

        // 알림 내용 스타일
        const content = notification.querySelector('.notification-content');
        content.style.cssText = `
            display: flex;
            align-items: center;
            gap: 12px;
        `;

        const icon = notification.querySelector('.notification-icon');
        icon.style.cssText = `
            font-size: 32px;
        `;

        const text = notification.querySelector('.notification-text');
        text.style.cssText = `
            flex: 1;
        `;

        const title = notification.querySelector('.notification-title');
        title.style.cssText = `
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 4px;
        `;

        const badgeName = notification.querySelector('.notification-badge');
        badgeName.style.cssText = `
            font-size: 16px;
            font-weight: 700;
        `;

        // 알림 표시
        document.body.appendChild(notification);
        
        // 애니메이션
        setTimeout(() => {
            notification.style.transform = 'translateX(0)';
        }, 100);

        // 자동 제거
        setTimeout(() => {
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.remove();
                }
            }, 300);
        }, 4000);
    }

    /**
     * 뱃지 표시/숨김 토글
     */
    async toggleBadgeDisplay(userBadgeId) {
        try {
            const response = await fetch(`/api/user-badges/${userBadgeId}/toggle-display`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const updatedBadge = await response.json();
                console.log('뱃지 표시 상태 변경:', updatedBadge);
                
                // 뱃지 컬렉션 새로고침
                await this.loadUserBadges();
                this.renderBadgeCollection();
                
                return updatedBadge;
            } else {
                console.error('뱃지 표시 상태 변경 실패:', response.status);
                return null;
            }
        } catch (error) {
            console.error('뱃지 표시 상태 변경 오류:', error);
            return null;
        }
    }

    /**
     * 뱃지 통계 가져오기
     */
    async getBadgeStats() {
        try {
            const response = await fetch('/api/user-badges/my-stats');
            if (response.ok) {
                return await response.json();
            } else {
                console.error('뱃지 통계 로드 실패:', response.status);
                return null;
            }
        } catch (error) {
            console.error('뱃지 통계 로드 오류:', error);
            return null;
        }
    }
}

// 전역 객체로 등록
window.badgeCollection = new BadgeCollection();

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    if (window.badgeCollection) {
        window.badgeCollection.init();
    }

    const refreshBtn = document.getElementById('badge-refresh-btn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', async () => {
            refreshBtn.disabled = true;
            refreshBtn.textContent = '⏳ 불러오는 중...';
            try {
                // 뱃지 지급 로직 실행
                const response = await fetch('/api/user-badges/refresh', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });
                const data = await response.json();
                console.log('🔄 뱃지 리로드 결과:', data);
                if (data && data.badges) {
                    window.badgeCollection.userBadges = data.badges;
                    window.badgeCollection.renderBadgeCollection();
                } else {
                    // fallback: 기존 방식
                    await window.badgeCollection.loadUserBadges();
                    window.badgeCollection.renderBadgeCollection();
                }
            } catch (e) {
                console.error('뱃지 리로드 실패:', e);
                await window.badgeCollection.loadUserBadges();
                window.badgeCollection.renderBadgeCollection();
            }
            refreshBtn.disabled = false;
            refreshBtn.textContent = '🔄 새로고침';
        });
    }
}); 