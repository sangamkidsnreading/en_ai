// admin.js - 음원 업로드 기능 포함 완성 버전

class AdminDashboard {
    constructor() {
        this.baseUrl = '/api/admin';
        this.currentTab = 'users';
        this.isSavingSentence = false;
        this.isSavingWord = false;
        this.isSavingUser = false;
        this.init();
    }

    init() {
        console.log('🔧 AdminDashboard 초기화 시작');
        this.bindEvents();
        this.bindNormalizeAudioFilenamesEvent();
        this.loadStats();
        this.loadUsers();
        console.log('✅ AdminDashboard 초기화 완료');
    }

    // 이벤트 바인딩
    bindEvents() {
        console.log('🔗 관리자 이벤트 바인딩 시작...');

        // 탭 전환
        const tabItems = document.querySelectorAll('.tab-item');
        console.log('탭 아이템 개수:', tabItems.length);

        const self = this; // this 바인딩 문제 해결

        tabItems.forEach(function(tab) {
            tab.addEventListener('click', function(e) {
                console.log('탭 클릭:', e.target.dataset.tab);
                self.switchTab(e.target.dataset.tab);
            });
        });

        // 검색
        const searchInput = document.getElementById('admin-search');
        if (searchInput) {
            searchInput.addEventListener('input', function(e) {
                console.log('검색 입력:', e.target.value);
                self.handleSearch(e.target.value);
            });
        } else {
            console.warn('검색 입력 요소를 찾을 수 없습니다.');
        }

        // 사용자 관리 버튼
        const addUserBtn = document.getElementById('add-user-btn');
        if (addUserBtn) {
            console.log('✅ 사용자 추가 버튼 찾음');
            addUserBtn.addEventListener('click', function(e) {
                console.log('🎯 사용자 추가 버튼 클릭됨');
                e.preventDefault();
                self.openUserModal();
            });
        } else {
            console.warn('❌ 사용자 추가 버튼을 찾을 수 없습니다.');
        }

        const saveUserBtn = document.getElementById('save-user-btn');
        if (saveUserBtn) {
            saveUserBtn.addEventListener('click', function(e) {
                console.log('🎯 사용자 저장 버튼 클릭됨');
                e.preventDefault();
                self.saveUser();
            });
        }

        // 단어 관리 버튼
        const addWordBtn = document.getElementById('add-word-btn');
        if (addWordBtn) {
            console.log('✅ 단어 추가 버튼 찾음');
            addWordBtn.addEventListener('click', function(e) {
                console.log('🎯 단어 추가 버튼 클릭됨');
                e.preventDefault();
                self.openWordModal();
            });
        }

        const saveWordBtn = document.getElementById('save-word-btn');
        if (saveWordBtn) {
            saveWordBtn.addEventListener('click', function(e) {
                console.log('🎯 단어 저장 버튼 클릭됨');
                e.preventDefault();
                self.saveWord();
            });
        }

        const uploadWordsBtn = document.getElementById('upload-words-btn');
        if (uploadWordsBtn) {
            uploadWordsBtn.addEventListener('click', function(e) {
                console.log('🎯 단어 엑셀 업로드 버튼 클릭됨');
                e.preventDefault();
                const fileInput = document.getElementById('word-file');
                if (fileInput) {
                    fileInput.click();
                }
            });
        }

        const wordFileInput = document.getElementById('word-file');
        if (wordFileInput) {
            wordFileInput.addEventListener('change', function(e) {
                console.log('단어 파일 선택됨:', e.target.files[0]);
                self.uploadWordsFile(e.target.files[0]);
            });
        }

        // 문장 관리 버튼
        const addSentenceBtn = document.getElementById('add-sentence-btn');
        if (addSentenceBtn) {
            console.log('✅ 문장 추가 버튼 찾음');
            addSentenceBtn.addEventListener('click', function(e) {
                console.log('🎯 문장 추가 버튼 클릭됨');
                e.preventDefault();
                self.openSentenceModal();
            });
        }

        const saveSentenceBtn = document.getElementById('save-sentence-btn');
        if (saveSentenceBtn) {
            saveSentenceBtn.removeEventListener('click', this.saveSentenceHandler);
            this.saveSentenceHandler = function(e) {
                console.log('🎯 문장 저장 버튼 클릭됨');
                e.preventDefault();
                e.stopPropagation();
                self.saveSentence();
            };
            saveSentenceBtn.addEventListener('click', this.saveSentenceHandler);
        }

        const uploadSentencesBtn = document.getElementById('upload-sentences-btn');
        if (uploadSentencesBtn) {
            uploadSentencesBtn.addEventListener('click', function(e) {
                console.log('🎯 문장 엑셀 업로드 버튼 클릭됨');
                e.preventDefault();
                const fileInput = document.getElementById('sentence-file');
                if (fileInput) {
                    fileInput.click();
                }
            });
        }

        const sentenceFileInput = document.getElementById('sentence-file');
        if (sentenceFileInput) {
            sentenceFileInput.addEventListener('change', function(e) {
                console.log('문장 파일 선택됨:', e.target.files[0]);
                self.uploadSentencesFile(e.target.files[0]);
            });
        }

        // 설정 저장
        const saveSettingsBtn = document.getElementById('save-settings-btn');
        if (saveSettingsBtn) {
            saveSettingsBtn.addEventListener('click', function(e) {
                console.log('🎯 설정 저장 버튼 클릭됨');
                e.preventDefault();
                self.saveSettings();
            });
        }

        // 선택 삭제 버튼 이벤트
        const deleteSelectedWordsBtn = document.getElementById('delete-selected-words-btn');
        if (deleteSelectedWordsBtn) {
            deleteSelectedWordsBtn.addEventListener('click', function(e) {
                console.log('🎯 선택된 단어 삭제 버튼 클릭됨');
                e.preventDefault();
                self.deleteSelectedWords();
            });
        }

        const deleteSelectedSentencesBtn = document.getElementById('delete-selected-sentences-btn');
        if (deleteSelectedSentencesBtn) {
            deleteSelectedSentencesBtn.addEventListener('click', function(e) {
                console.log('🎯 선택된 문장 삭제 버튼 클릭됨');
                e.preventDefault();
                self.deleteSelectedSentences();
            });
        }

        // 음원 관련 이벤트 바인딩
        this.bindAudioEvents();

        // 모달 닫기
        document.querySelectorAll('.close, .modal-cancel').forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                console.log('모달 닫기 버튼 클릭됨');
                e.preventDefault();
                const modalId = e.target.dataset.modal || e.target.closest('.modal').id;
                self.closeModal(modalId);
            });
        });

        // 모달 배경 클릭 시 닫기
        document.querySelectorAll('.modal').forEach(function(modal) {
            modal.addEventListener('click', function(e) {
                if (e.target === modal) {
                    console.log('모달 배경 클릭됨');
                    self.closeModal(modal.id);
                }
            });
        });

        console.log('✅ 관리자 이벤트 바인딩 완료');
    }

    // 음원 관련 이벤트 바인딩
    bindAudioEvents() {
        const self = this;
        console.log('🎵 음원 이벤트 바인딩 시작...');

        // 단어 모달 음원 업로드
        const wordAudioUploadBtn = document.getElementById('word-audio-upload-btn');
        const wordAudioFile = document.getElementById('word-audio-file');
        const wordAudioRemoveBtn = document.getElementById('word-audio-remove-btn');
        const wordAudioReplaceBtn = document.getElementById('word-audio-replace-btn');

        if (wordAudioUploadBtn && wordAudioFile) {
            wordAudioUploadBtn.addEventListener('click', function() {
                wordAudioFile.click();
            });

            wordAudioFile.addEventListener('change', function(e) {
                if (e.target.files.length > 0) {
                self.handleAudioFileSelection(e.target.files[0], 'word');
                }
            });
        }

        if (wordAudioRemoveBtn) {
            wordAudioRemoveBtn.addEventListener('click', function() {
                self.removeSelectedAudio('word');
            });
        }

        if (wordAudioReplaceBtn) {
            wordAudioReplaceBtn.addEventListener('click', function() {
                wordAudioFile.click();
            });
        }

        // 문장 모달 음원 업로드
        const sentenceAudioUploadBtn = document.getElementById('sentence-audio-upload-btn');
        const sentenceAudioFile = document.getElementById('sentence-audio-file');
        const sentenceAudioRemoveBtn = document.getElementById('sentence-audio-remove-btn');
        const sentenceAudioReplaceBtn = document.getElementById('sentence-audio-replace-btn');

        if (sentenceAudioUploadBtn && sentenceAudioFile) {
            sentenceAudioUploadBtn.addEventListener('click', function() {
                sentenceAudioFile.click();
            });

            sentenceAudioFile.addEventListener('change', function(e) {
                if (e.target.files.length > 0) {
                self.handleAudioFileSelection(e.target.files[0], 'sentence');
                }
            });
        }

        if (sentenceAudioRemoveBtn) {
            sentenceAudioRemoveBtn.addEventListener('click', function() {
                self.removeSelectedAudio('sentence');
            });
        }

        if (sentenceAudioReplaceBtn) {
            sentenceAudioReplaceBtn.addEventListener('click', function() {
                sentenceAudioFile.click();
            });
        }

        // 음원 일괄 업로드 버튼
        const bulkAudioUploadWordsBtn = document.getElementById('bulk-audio-upload-words-btn');
        const bulkAudioUploadSentencesBtn = document.getElementById('bulk-audio-upload-sentences-btn');

        if (bulkAudioUploadWordsBtn) {
            bulkAudioUploadWordsBtn.addEventListener('click', function() {
                self.openBulkAudioModal('words');
            });
        }

        if (bulkAudioUploadSentencesBtn) {
            bulkAudioUploadSentencesBtn.addEventListener('click', function() {
                self.openBulkAudioModal('sentences');
            });
        }

        // 음원 관리 탭 버튼들
        const audioStatsBtn = document.getElementById('audio-stats-btn');
        const missingAudioBtn = document.getElementById('missing-audio-btn');
        const applyAudioFilterBtn = document.getElementById('apply-audio-filter');

        if (audioStatsBtn) {
            audioStatsBtn.addEventListener('click', function() {
                self.showAudioStats();
            });
        }

        if (missingAudioBtn) {
            missingAudioBtn.addEventListener('click', function() {
                self.findMissingAudio();
            });
        }

        if (applyAudioFilterBtn) {
            applyAudioFilterBtn.addEventListener('click', function() {
                self.applyAudioFilter();
            });
        }

        // 음원 일괄 업로드 모달
        const startBulkUploadBtn = document.getElementById('start-bulk-upload-btn');
        const bulkAudioZipFile = document.getElementById('bulk-audio-zip-file');

        if (startBulkUploadBtn) {
            startBulkUploadBtn.addEventListener('click', function() {
                self.startBulkAudioUpload();
            });
        }

        if (bulkAudioZipFile) {
            bulkAudioZipFile.addEventListener('change', function(e) {
                console.log('음원 일괄 업로드 파일 선택됨:', e.target.files[0]);
            });
        }

        console.log('✅ 음원 이벤트 바인딩 완료');
    }

    // 음원 파일 선택 처리
    handleAudioFileSelection(file, type) {
        console.log('🎵 음원 파일 처리 시작:', file, type);
        
        if (!file) {
            console.warn('파일이 없습니다.');
            return;
        }

        // 파일 크기 검증 (10MB)
        if (file.size > 10 * 1024 * 1024) {
            this.showError('음원 파일 크기는 10MB 이하여야 합니다.');
            return;
        }

        // 파일 형식 검증
        const validTypes = ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/mp3', 'audio/mp4', 'audio/x-m4a'];
        if (!validTypes.includes(file.type)) {
            this.showError('MP3, M4A, WAV, OGG 형식의 음원 파일만 업로드 가능합니다.');
            return;
        }

        // 미리보기 생성
        const audioInfo = document.getElementById(type + '-audio-info');
        const audioPreview = document.getElementById(type + '-audio-preview');
        const audioFileName = document.getElementById(type + '-audio-file-name');
        const currentAudio = document.getElementById(type + '-current-audio');

        if (audioInfo && audioPreview && audioFileName) {
            // 파일 URL 생성
            const fileURL = URL.createObjectURL(file);
            
            // 미리보기 설정
            audioPreview.src = fileURL;
            audioFileName.textContent = file.name;
            
            // UI 표시 업데이트
            audioInfo.style.display = 'block';
            if (currentAudio) {
                currentAudio.style.display = 'none';
            }

            console.log('✅ ' + type + ' 음원 파일 미리보기 설정 완료:', file.name);
        } else {
            console.error('음원 미리보기 요소를 찾을 수 없습니다:', type);
        }
    }

    // 선택된 음원 제거
    removeSelectedAudio(type) {
        console.log('🎵 음원 제거:', type);
        
        const audioInfo = document.getElementById(type + '-audio-info');
        const audioFile = document.getElementById(type + '-audio-file');
        const audioPreview = document.getElementById(type + '-audio-preview');
        const currentAudio = document.getElementById(type + '-current-audio');

        if (audioInfo && audioFile && audioPreview) {
            // 파일 입력 초기화
            audioFile.value = '';
            
            // 미리보기 초기화
            audioPreview.src = '';
            audioInfo.style.display = 'none';
            
            // 기존 음원이 있다면 다시 표시
            if (currentAudio) {
                currentAudio.style.display = 'block';
            }

            console.log('✅ ' + type + ' 음원 파일 제거 완료');
        }
    }

    // API 호출 헬퍼 (Promise 기반)
    apiCall(endpoint, method, data) {
        method = method || 'GET';
        data = data || null;

        const options = {
            method: method,
            headers: {
                'Content-Type': 'application/json',
            }
        };

        if (data) {
            options.body = JSON.stringify(data);
        }

        return fetch(this.baseUrl + endpoint, options)
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
            })
            .catch(function(error) {
                console.error('API 호출 오류:', error);
                throw error;
            });
    }

    // 파일 업로드 API 호출 (Promise 기반)
    uploadFile(endpoint, file) {
        const formData = new FormData();
        formData.append('file', file);

        return fetch(this.baseUrl + endpoint, {
            method: 'POST',
            body: formData
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
            })
            .catch(function(error) {
                console.error('파일 업로드 오류:', error);
                throw error;
            });
    }

    // === 파일명 정규화 함수 추가 ===
    normalizeAudioFileName(originalName) {
        const ext = originalName.substring(originalName.lastIndexOf('.'));
        let base = originalName.replace(ext, '');
        let m = base.match(/문장\s*(\d+)번/);
        if (m) return `sentence${m[1]}${ext}`;
        m = base.match(/(\d+)번/);
        if (m) return `no${m[1]}${ext}`;
        return originalName;
    }

    // === uploadAudioFile 함수 내 파일명 변환 적용 ===
    uploadAudioFile(itemId, file, type) {
        // 원본 파일명 그대로 사용 (정규화하지 않음)
        const newFile = new File([file], file.name, { type: file.type });
        const formData = new FormData();
        formData.append('file', newFile);
        const endpoint = type === 'word' ? 
            '/words/' + itemId + '/audio' : 
            '/sentences/' + itemId + '/audio';
        return fetch(this.baseUrl + endpoint, {
            method: 'POST',
            body: formData
        })
        .then(function(response) {
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            return response.json();
        })
        .then(function(result) {
            console.log('음원 업로드 성공:', result);
            return result;
        })
        .catch(function(error) {
            console.error('음원 업로드 실패:', error);
            throw error;
        });
    }

    // 통계 로드 (Promise 기반)
    loadStats() {
        const self = this;

        this.apiCall('/stats')
            .then(function(stats) {
                const totalUsersEl = document.getElementById('total-users');
                const totalWordsEl = document.getElementById('total-words');
                const totalSentencesEl = document.getElementById('total-sentences');
                const activeUsersEl = document.getElementById('active-users');

                if (totalUsersEl) totalUsersEl.textContent = stats.totalUsers || 0;
                if (totalWordsEl) totalWordsEl.textContent = stats.totalWords || 0;
                if (totalSentencesEl) totalSentencesEl.textContent = stats.totalSentences || 0;
                if (activeUsersEl) activeUsersEl.textContent = stats.activeUsers || 0;
            })
            .catch(function(error) {
                console.error('통계 로드 실패:', error);

                // 임시 더미 데이터로 대체
                const dummyStats = {
                    totalUsers: 2,
                    totalWords: 20,
                    totalSentences: 10,
                    activeUsers: 1
                };

                const totalUsersEl = document.getElementById('total-users');
                const totalWordsEl = document.getElementById('total-words');
                const totalSentencesEl = document.getElementById('total-sentences');
                const activeUsersEl = document.getElementById('active-users');

                if (totalUsersEl) totalUsersEl.textContent = dummyStats.totalUsers;
                if (totalWordsEl) totalWordsEl.textContent = dummyStats.totalWords;
                if (totalSentencesEl) totalSentencesEl.textContent = dummyStats.totalSentences;
                if (activeUsersEl) activeUsersEl.textContent = dummyStats.activeUsers;

                self.showWarning('통계 데이터를 불러올 수 없어 기본값을 표시합니다.');
            });
    }

    // 탭 전환
    switchTab(tabName) {
        console.log('탭 전환:', tabName);

        // 탭 헤더 업데이트
        document.querySelectorAll('.tab-item').forEach(function(tab) {
            tab.classList.remove('active');
        });
        const activeTab = document.querySelector('[data-tab="' + tabName + '"]');
        if (activeTab) {
            activeTab.classList.add('active');
        }

        // 탭 콘텐츠 업데이트
        document.querySelectorAll('.tab-content').forEach(function(content) {
            content.classList.remove('active');
        });
        const activeContent = document.getElementById(tabName + '-tab');
        if (activeContent) {
            activeContent.classList.add('active');
        }

        this.currentTab = tabName;

        // 해당 탭 데이터 로드
        switch (tabName) {
            case 'users':
                this.loadUsers();
                break;
            case 'words':
                this.loadWords();
                break;
            case 'sentences':
                this.loadSentences();
                break;
            case 'audio':
                this.loadAudioManagement();
                break;
            case 'settings':
                this.loadSettings();
                break;
        }
    }

    // 검색 처리 (Promise 기반)
    handleSearch(query) {
        const self = this;

        if (query.trim() === '') {
            // 전체 데이터 다시 로드
            switch (this.currentTab) {
                case 'users':
                    this.loadUsers();
                    break;
                case 'words':
                    this.loadWords();
                    break;
                case 'sentences':
                    this.loadSentences();
                    break;
            }
            return;
        }

        let endpoint;
        switch (this.currentTab) {
            case 'users':
                endpoint = '/users/search?query=' + encodeURIComponent(query);
                break;
            case 'words':
                endpoint = '/words/search?query=' + encodeURIComponent(query);
                break;
            case 'sentences':
                endpoint = '/sentences/search?query=' + encodeURIComponent(query);
                break;
            default:
                return;
        }

        this.apiCall(endpoint)
            .then(function(results) {
                switch (self.currentTab) {
                    case 'users':
                        self.renderUsers(results);
                        break;
                    case 'words':
                        self.renderWords(results);
                        break;
                    case 'sentences':
                        self.renderSentences(results);
                        break;
                }
            })
            .catch(function(error) {
                console.error('검색 실패:', error);
            });
    }

    // ========== 사용자 관리 ==========

    loadUsers() {
        const self = this;

        this.apiCall('/users')
            .then(function(users) {
                self.renderUsers(users);
            })
            .catch(function(error) {
                console.error('사용자 로드 실패:', error);
                // 테스트용 더미 데이터
                self.renderUsers([
                    { id: 1, name: '김학생', email: 'student@test.com', role: 'STUDENT' },
                    { id: 2, name: '이관리자', email: 'admin@test.com', role: 'ADMIN' }
                ]);
            });
    }

    renderUsers(users) {
        const userList = document.getElementById('user-list');

        if (!userList) {
            console.warn('user-list 요소를 찾을 수 없습니다.');
            return;
        }

        if (users.length === 0) {
            userList.innerHTML = '<div class="no-data">등록된 사용자가 없습니다.</div>';
            return;
        }

        userList.innerHTML = users.map(function(user) {
            const statusClass = user.isActive === false ? 'inactive' : 'active';
            const statusText = user.isActive === false ? '비활성' : '활성';

            return '<div class="user-item ' + statusClass + '" data-user-id="' + user.id + '">' +
                '<div class="user-info">' +
                '<div class="user-name">' + user.name + '</div>' +
                '<div class="user-email">' + user.email + '</div>' +
                (user.username && user.username !== user.email ?
                    '<div class="user-username">@' + user.username + '</div>' : '') +
                '<div class="user-role ' + user.role.toLowerCase() + '">' +
                (user.role === 'ADMIN' ? '관리자' : '학생') + '</div>' +
                '<div class="user-status ' + statusClass + '">' + statusText + '</div>' +
                '</div>' +
                '<div class="user-actions">' +
                '<button class="edit-btn" onclick="window.adminDashboard.editUser(' + user.id + ')">수정</button>' +
                '<button class="delete-btn" onclick="window.adminDashboard.deleteUser(' + user.id + ')">삭제</button>' +
                '</div>' +
                '</div>';
        }).join('');
    }

    openUserModal(user) {
        user = user || null;
        console.log('🔓 사용자 모달 열기:', user);
        const modal = document.getElementById('user-modal');
        const title = document.getElementById('user-modal-title');

        if (!modal) {
            console.error('❌ 사용자 모달을 찾을 수 없습니다.');
            return;
        }

        if (user) {
            title.textContent = '사용자 수정';
            document.getElementById('user-id').value = user.id;

            const usernameField = document.getElementById('user-username');
            if (usernameField) {
                usernameField.value = user.username || '';
            }

            document.getElementById('user-name').value = user.name;
            document.getElementById('user-email').value = user.email;
            document.getElementById('user-role').value = user.role;

            const activeField = document.getElementById('user-active');
            if (activeField) {
                activeField.checked = user.isActive !== false;
            }

            document.getElementById('user-password').required = false;
            document.getElementById('user-password').value = '';
        } else {
            title.textContent = '사용자 추가';
            document.getElementById('user-id').value = '';

            const usernameField = document.getElementById('user-username');
            if (usernameField) {
                usernameField.value = '';
            }

            document.getElementById('user-name').value = '';
            document.getElementById('user-email').value = '';
            document.getElementById('user-role').value = 'STUDENT';

            const activeField = document.getElementById('user-active');
            if (activeField) {
                activeField.checked = true;
            }

            document.getElementById('user-password').value = '';
            document.getElementById('user-password').required = true;
        }

        modal.style.display = 'block';
        console.log('✅ 사용자 모달이 열렸습니다.');
    }

    editUser(userId) {
        const self = this;

        this.apiCall('/users')
            .then(function(users) {
                const user = users.find(function(u) { return u.id === userId; });
                if (user) {
                    self.openUserModal(user);
                }
            })
            .catch(function(error) {
                console.error('사용자 정보 로드 실패:', error);
            });
    }

    saveUser() {
        const self = this;
        const userId = document.getElementById('user-id').value;

        const usernameField = document.getElementById('user-username');
        const activeField = document.getElementById('user-active');
        const emailValue = document.getElementById('user-email').value;

        const userData = {
            username: usernameField && usernameField.value.trim()
                ? usernameField.value.trim()
                : emailValue,
            name: document.getElementById('user-name').value,
            email: emailValue,
            role: document.getElementById('user-role').value,
            password: document.getElementById('user-password').value,
            isActive: activeField ? activeField.checked : true
        };

        // 필수 필드 검증
        if (!userData.name || !userData.email || (!userId && !userData.password)) {
            this.showError('필수 항목을 모두 입력해주세요.');
            return;
        }

        // 이메일 형식 검증
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(userData.email)) {
            this.showError('올바른 이메일 형식을 입력해주세요.');
            return;
        }

        const endpoint = userId ? '/users/' + userId : '/users';
        const method = userId ? 'PUT' : 'POST';

        this.apiCall(endpoint, method, userData)
            .then(function(response) {
                self.showSuccess(userId ? '사용자가 수정되었습니다.' : '사용자가 추가되었습니다.');
                self.closeModal('user-modal');
                self.loadUsers();
                self.loadStats();
            })
            .catch(function(error) {
                console.error('사용자 저장 실패:', error);
                let errorMessage = '사용자 저장에 실패했습니다.';
                if (error.message && error.message.includes('username')) {
                    errorMessage = '사용자명이 올바르지 않습니다.';
                } else if (error.message && error.message.includes('email')) {
                    errorMessage = '이미 사용 중인 이메일입니다.';
                }
                self.showError(errorMessage);
            });
    }

    deleteUser(userId) {
        const self = this;

        if (!confirm('정말로 이 사용자를 삭제하시겠습니까?')) {
            return;
        }

        this.apiCall('/users/' + userId, 'DELETE')
            .then(function() {
                self.showSuccess('사용자가 삭제되었습니다.');
                self.loadUsers();
                self.loadStats();
            })
            .catch(function(error) {
                console.error('사용자 삭제 실패:', error);
                self.showError('사용자 삭제에 실패했습니다.');
            });
    }

    // ========== 단어 관리 ==========

    loadWords() {
        const self = this;
        this.showLoading('단어 목록을 불러오는 중...');

        this.apiCall('/words')
            .then(function(words) {
                console.log('단어 로드 성공:', words.length + '개');
                self.renderWords(words);
                self.hideLoading();
            })
            .catch(function(error) {
                console.error('단어 로드 실패:', error);
                self.hideLoading();

                const dummyWords = [
                    {
                        id: 1,
                        english: 'apple',
                        korean: '사과',
                        level: 1,
                        pronunciation: 'æpl',
                        audioUrl: null
                    },
                    {
                        id: 2,
                        english: 'book',
                        korean: '책',
                        level: 1,
                        pronunciation: 'bʊk',
                        audioUrl: null
                    }
                ];

                self.renderWords(dummyWords);
                self.showWarning('서버에서 단어 데이터를 불러올 수 없어 테스트 데이터를 표시합니다.');
            });
    }

    renderWords(words) {
        const wordList = document.getElementById('word-list');
        if (!wordList) {
            console.warn('word-list 요소를 찾을 수 없습니다.');
            return;
        }
        if (words.length === 0) {
            wordList.innerHTML = '<div class="no-data">등록된 단어가 없습니다.</div>';
            return;
        }
        wordList.innerHTML = words.map(function(word) {
            const audioIcon = word.audioUrl ? '' : '🔇';
            const audioClass = word.audioUrl ? 'word-audio' : 'word-audio-missing';
            return '<div class="word-item" data-word-id="' + word.id + '" data-word-day="' + (word.day || 1) + '">' +
                '<input type="checkbox" class="word-checkbox" data-word-id="' + word.id + '">' +
                '<div class="word-content">' +
                '<div class="word-info">' +
                '<div class="word-english">' + word.english + '</div>' +
                '<div class="word-korean">' + word.korean + '</div>' +
                '<div class="word-details">' +
                '<span class="word-level">Level ' + word.level + '</span>' +
                (word.pronunciation ? '<span class="word-pronunciation">[' + word.pronunciation + ']</span>' : '') +
                '<span class="word-day">Day ' + (word.day || 1) + '</span>' + // day 정보 표시
                '<span class="' + audioClass + '">' + audioIcon + '</span>' +
                '</div>' +
                (word.audioUrl ? 
                    '<div class="word-audio-player">' +
                    '<audio controls style="width: 100%; max-width: 300px; margin-top: 8px;">' +
                    '<source src="' + word.audioUrl + '">' +
                    '</audio></div>' : ''
                ) +
                '</div>' +
                '<div class="word-actions">' +
                '<button class="edit-btn" onclick="window.adminDashboard.editWord(' + word.id + ')">수정</button>' +
                '<button class="audio-btn" onclick="window.adminDashboard.uploadWordAudio(' + word.id + ')">' +
                (word.audioUrl ? '음원 교체' : '음원 업로드') +
                '</button>' +
                (word.audioUrl ? 
                    '<button class="delete-btn" onclick="window.adminDashboard.deleteWordAudio(' + word.id + ')">음원 삭제</button>' : ''
                ) +
                '<button class="delete-btn" onclick="window.adminDashboard.deleteWord(' + word.id + ')">삭제</button>' +
                '</div>' +
                '</div>' +
                '</div>';
        }).join('');

        // 체크박스 이벤트 바인딩
        this.bindWordCheckboxEvents();
        this.bindSelectAllWordEvents();
    }

    openWordModal(word) {
        word = word || null;
        console.log('🔓 단어 모달 열기:', word);
        const modal = document.getElementById('word-modal');
        const title = document.getElementById('word-modal-title');

        if (!modal) {
            console.error('❌ 단어 모달을 찾을 수 없습니다.');
            return;
        }

        if (word) {
            title.textContent = '단어 수정';
            document.getElementById('word-id').value = word.id;
            document.getElementById('word-english').value = word.english;
            document.getElementById('word-korean').value = word.korean;
            document.getElementById('word-level').value = word.level;
            document.getElementById('word-pronunciation').value = word.pronunciation || '';
            // day 값 select에 반영
            if (word.day) {
                document.getElementById('word-day').value = word.day;
            }
        } else {
            title.textContent = '단어 추가';
            document.getElementById('word-id').value = '';
            document.getElementById('word-english').value = '';
            document.getElementById('word-korean').value = '';
            document.getElementById('word-level').value = '1';
            document.getElementById('word-pronunciation').value = '';
            document.getElementById('word-day').value = '1'; // 추가: 신규는 1로 초기화

            // 음원 관련 초기화
            const currentAudio = document.getElementById('word-current-audio');
            const audioInfo = document.getElementById('word-audio-info');
            const audioFile = document.getElementById('word-audio-file');
            
            if (currentAudio) currentAudio.style.display = 'none';
            if (audioInfo) audioInfo.style.display = 'none';
            if (audioFile) audioFile.value = '';
        }

        modal.style.display = 'block';
        console.log('✅ 단어 모달이 열렸습니다.');
    }

    editWord(wordId) {
        const self = this;
        const wordItem = document.querySelector('[data-word-id="' + wordId + '"]');
        if (wordItem) {
            const englishEl = wordItem.querySelector('.word-english');
            const koreanEl = wordItem.querySelector('.word-korean');
            const levelEl = wordItem.querySelector('.word-level');
            const pronunciationEl = wordItem.querySelector('.word-pronunciation');
            const word = {
                id: wordId,
                english: englishEl ? englishEl.textContent : '',
                korean: koreanEl ? koreanEl.textContent : '',
                level: levelEl ? parseInt(levelEl.textContent.replace('Level ', '')) : 1,
                pronunciation: pronunciationEl ? pronunciationEl.textContent.replace(/\[|\]/g, '') : '',
                day: wordItem.getAttribute('data-word-day') ? Number(wordItem.getAttribute('data-word-day')) : 1
            };
            this.openWordModal(word);
        } else {
            this.apiCall('/words')
                .then(function(words) {
                    const word = words.find(function(w) { return w.id === wordId; });
                    if (word) {
                        self.openWordModal(word);
                    } else {
                        self.showError('단어 정보를 찾을 수 없습니다.');
                    }
                })
                .catch(function(error) {
                    console.error('단어 정보 로드 실패:', error);
                    self.showError('단어 정보를 불러오는데 실패했습니다.');
                });
        }
    }

    saveWord() {
        const self = this;

        if (this.isSavingWord) {
            console.warn('단어 저장이 이미 진행 중입니다.');
            return;
        }

        this.isSavingWord = true;

        const wordId = document.getElementById('word-id').value;
        const audioFile = document.getElementById('word-audio-file').files[0];

        const wordData = {
            english: document.getElementById('word-english').value.trim(),
            korean: document.getElementById('word-korean').value.trim(),
            level: parseInt(document.getElementById('word-level').value),
            pronunciation: document.getElementById('word-pronunciation').value.trim(),
            day: Number(document.getElementById('word-day').value) // day 필드 추가
        };

        // 필수 필드 검증
        if (!wordData.english || !wordData.korean) {
            this.showError('영어 단어와 한국어 뜻을 모두 입력해주세요.');
            this.isSavingWord = false;
            return;
        }

        const endpoint = wordId ? '/words/' + wordId : '/words';
        const method = wordId ? 'PUT' : 'POST';

        this.showLoading(wordId ? '단어를 수정하는 중...' : '단어를 추가하는 중...');

        this.apiCall(endpoint, method, wordData)
            .then(function(savedWord) {
                // 단어 저장 성공 후 음원 업로드
                if (audioFile && savedWord.id) {
                    return self.uploadAudioFile(savedWord.id, audioFile, 'word');
                }
                return Promise.resolve();
            })
            .then(function() {
                self.hideLoading();
                self.showSuccess(wordId ? '단어가 수정되었습니다.' : '단어가 추가되었습니다.');
                self.closeModal('word-modal');
                self.loadWords();
                self.loadStats();
                self.isSavingWord = false;
            })
            .catch(function(error) {
                console.error('단어 저장 실패:', error);
                self.hideLoading();
                self.showError(error.message || '단어 저장에 실패했습니다.');
                self.isSavingWord = false;
            });
    }

    deleteWord(wordId) {
        const self = this;

        if (!confirm('정말로 이 단어를 삭제하시겠습니까?')) {
            return;
        }

        this.showLoading('단어를 삭제하는 중...');

        this.apiCall('/words/' + wordId, 'DELETE')
            .then(function() {
                self.hideLoading();
                self.showSuccess('단어가 삭제되었습니다.');
                self.loadWords();
                self.loadStats();
            })
            .catch(function(error) {
                console.error('단어 삭제 실패:', error);
                self.hideLoading();
                self.showError('단어 삭제에 실패했습니다.');
            });
    }

    async uploadWordAudio(wordId) {
        const fileInput = document.getElementById('word-audio-file');
        const file = fileInput.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`/api/admin/words/${wordId}/audio`, {
            method: 'POST',
            body: formData
        });

        if (response.ok) {
            const data = await response.json();
            // audioUrl을 모달에 표시
            const audioPreview = document.getElementById('word-audio-preview');
            if (audioPreview) {
                audioPreview.innerHTML = `<audio src="${data.audioUrl}" controls></audio>`;
            }
            alert('업로드 성공!');
        } else {
            alert('업로드 실패!');
        }
    }

    uploadWordAudio(wordId) {
        const self = this;

        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'audio/*';

        input.onchange = function(e) {
            const file = e.target.files[0];
            if (!file) return;

            if (file.size > 10 * 1024 * 1024) {
                self.showError('파일 크기는 10MB 이하여야 합니다.');
                return;
            }

            if (!file.type.startsWith('audio/')) {
                self.showError('음성 파일만 업로드할 수 있습니다.');
                return;
            }

            self.showLoading('음성 파일을 업로드하는 중...');

            self.uploadAudioFile(wordId, file, 'word')
                .then(function(response) {
                    self.hideLoading();
                    self.showSuccess('음성 파일이 업로드되었습니다.');
                    self.loadWords();
                })
                .catch(function(error) {
                    console.error('음성 파일 업로드 실패:', error);
                    self.hideLoading();
                    self.showError('음성 파일 업로드에 실패했습니다.');
                });
        };

        input.click();
    }

    deleteWordAudio(wordId) {
        const self = this;

        if (!confirm('이 음원을 삭제하시겠습니까?')) {
            return;
        }

        this.apiCall('/words/' + wordId + '/audio', 'DELETE')
            .then(function() {
                self.showSuccess('음원이 삭제되었습니다.');
                self.loadWords();
            })
            .catch(function(error) {
                console.error('음원 삭제 실패:', error);
                self.showError('음원 삭제에 실패했습니다.');
            });
    }

    uploadWordsFile(file) {
        const self = this;

        if (!file) {
            this.showError('파일을 선택해주세요.');
            return;
        }

        const validExtensions = ['.xlsx', '.xls'];
        const fileName = file.name.toLowerCase();
        const isValidFile = validExtensions.some(function(ext) {
            return fileName.endsWith(ext);
        });

        if (!isValidFile) {
            this.showError('엑셀 파일(.xlsx, .xls)만 업로드할 수 있습니다.');
            return;
        }

        if (file.size > 50 * 1024 * 1024) {
            this.showError('파일 크기는 50MB 이하여야 합니다.');
            return;
        }

        this.showLoading('단어 파일을 업로드하고 처리하는 중...');

        this.uploadFile('/words/bulk-upload', file)
            .then(function(response) {
                self.hideLoading();

                const successCount = response.successCount || 0;
                const errorCount = response.errorCount || 0;

                let message = successCount + '개의 단어가 성공적으로 등록되었습니다.';
                if (errorCount > 0) {
                    message += '\n' + errorCount + '개의 단어 등록에 실패했습니다.';
                }

                if (errorCount > 0) {
                    self.showWarning(message);
                } else {
                    self.showSuccess(message);
                }

                self.loadWords();
                self.loadStats();

                const fileInput = document.getElementById('word-file');
                if (fileInput) {
                    fileInput.value = '';
                }
            })
            .catch(function(error) {
                console.error('단어 파일 업로드 실패:', error);
                self.hideLoading();
                self.showError('단어 파일 업로드에 실패했습니다: ' + error.message);

                const fileInput = document.getElementById('word-file');
                if (fileInput) {
                    fileInput.value = '';
                }
            });
    }

    // ========== 문장 관리 ==========

    loadSentences() {
        const self = this;
        this.showLoading('문장 목록을 불러오는 중...');

        this.apiCall('/sentences')
            .then(function(sentences) {
                console.log('문장 로드 성공:', sentences.length + '개');
                self.renderSentences(sentences);
                self.hideLoading();
            })
            .catch(function(error) {
                console.error('문장 로드 실패:', error);
                self.hideLoading();

                const dummySentences = [
                    {
                        id: 1,
                        english: 'I love reading books.',
                        korean: '나는 책 읽는 것을 좋아한다.',
                        level: 1,
                        audioUrl: null
                    },
                    {
                        id: 2,
                        english: 'How are you today?',
                        korean: '오늘 어떻게 지내세요?',
                        level: 1,
                        audioUrl: null
                    }
                ];

                self.renderSentences(dummySentences);
                self.showWarning('서버에서 문장 데이터를 불러올 수 없어 테스트 데이터를 표시합니다.');
            });
    }

    renderSentences(sentences) {
        const sentenceList = document.getElementById('sentence-list');

        if (!sentenceList) {
            console.warn('sentence-list 요소를 찾을 수 없습니다.');
            return;
        }

        if (sentences.length === 0) {
            sentenceList.innerHTML = '<div class="no-data">등록된 문장이 없습니다.</div>';
            return;
        }

        // 중복 제거를 위해 ID 기준으로 필터링
        const uniqueSentences = sentences.filter((sentence, index, self) => 
            index === self.findIndex(s => s.id === sentence.id)
        );

        sentenceList.innerHTML = uniqueSentences.map(function(sentence) {
            const koreanText = sentence.translation || sentence.korean || '';
            const audioIcon = sentence.audioUrl ? '' : '🔇';
            const audioClass = sentence.audioUrl ? 'sentence-audio' : 'sentence-audio-missing';

            return '<div class="sentence-item" data-sentence-id="' + sentence.id + '">' +
                '<input type="checkbox" class="sentence-checkbox" data-sentence-id="' + sentence.id + '">' +
                '<div class="sentence-content" style="display: flex; align-items: flex-start; gap: 24px;">' +
                    '<div class="sentence-info">' +
                        '<div class="sentence-english">' + sentence.english + '</div>' +
                        '<div class="sentence-korean">' + koreanText + '</div>' +
                        '<div class="sentence-details">' +
                            '<span class="sentence-level">Level ' + sentence.level + '</span>' +
                            '<span class="sentence-day">Day ' + (sentence.day || '-') + '</span>' +
                            '<span class="' + audioClass + '">' + audioIcon + '</span>' +
                        '</div>' +
                        (sentence.audioUrl ? 
                            '<div class="sentence-audio-player">' +
                                '<audio controls style="width: 100%; max-width: 220px;">' +
                                    '<source src="' + sentence.audioUrl + '">' +
                                '</audio></div>' :
                            '<div class="sentence-audio-player" style="width:220px;"></div>'
                        ) +
                    '</div>' +
                    '<div class="sentence-actions" style="margin-left:auto; display:flex; gap:8px;">' +
                        '<button class="edit-btn" onclick="window.adminDashboard.editSentence(' + sentence.id + ')">수정</button>' +
                        '<button class="audio-btn" onclick="window.adminDashboard.uploadSentenceAudio(' + sentence.id + ')">' +
                        (sentence.audioUrl ? '음원 교체' : '음원 업로드') +
                        '</button>' +
                        (sentence.audioUrl ? 
                            '<button class="delete-btn" onclick="window.adminDashboard.deleteSentenceAudio(' + sentence.id + ')">음원 삭제</button>' : ''
                        ) +
                        '<button class="delete-btn" onclick="window.adminDashboard.deleteSentence(' + sentence.id + ')">삭제</button>' +
                    '</div>' +
                '</div>' +
            '</div>';
        }).join('');

        // 체크박스 이벤트 바인딩
        this.bindSentenceCheckboxEvents();
        this.bindSelectAllSentenceEvents();
    }

    openSentenceModal(sentence) {
        sentence = sentence || null;
        console.log('🔓 문장 모달 열기:', sentence);
        const modal = document.getElementById('sentence-modal');
        const title = document.getElementById('sentence-modal-title');

        if (!modal) {
            console.error('❌ 문장 모달을 찾을 수 없습니다.');
            return;
        }

        if (sentence) {
            title.textContent = '문장 수정';
            document.getElementById('sentence-id').value = sentence.id;
            document.getElementById('sentence-english').value = sentence.english;
            document.getElementById('sentence-korean').value = sentence.translation || sentence.korean || '';
            document.getElementById('sentence-level').value = sentence.level;
            // day 값 select에 반영
            if (sentence.day) {
                document.getElementById('sentence-day').value = sentence.day;
            }
        } else {
            title.textContent = '문장 추가';
            document.getElementById('sentence-id').value = '';
            document.getElementById('sentence-english').value = '';
            document.getElementById('sentence-korean').value = '';
            document.getElementById('sentence-level').value = '1';
            document.getElementById('sentence-day').value = '1'; // 추가: 신규는 1로 초기화

            // 음원 관련 초기화
            const currentAudio = document.getElementById('sentence-current-audio');
            const audioInfo = document.getElementById('sentence-audio-info');
            const audioFile = document.getElementById('sentence-audio-file');
            
            if (currentAudio) currentAudio.style.display = 'none';
            if (audioInfo) audioInfo.style.display = 'none';
            if (audioFile) audioFile.value = '';
        }

        modal.style.display = 'block';
        console.log('✅ 문장 모달이 열렸습니다.');
    }

    editSentence(sentenceId) {
        const self = this;

        const sentenceItem = document.querySelector('[data-sentence-id="' + sentenceId + '"]');
        if (sentenceItem) {
            const englishEl = sentenceItem.querySelector('.sentence-english');
            const koreanEl = sentenceItem.querySelector('.sentence-korean');
            const levelEl = sentenceItem.querySelector('.sentence-level');

            const sentence = {
                id: sentenceId,
                english: englishEl ? englishEl.textContent : '',
                korean: koreanEl ? koreanEl.textContent : '',
                level: levelEl ? parseInt(levelEl.textContent.replace('Level ', '')) : 1
            };

            this.openSentenceModal(sentence);
        } else {
            this.apiCall('/sentences')
                .then(function(sentences) {
                    const sentence = sentences.find(function(s) { return s.id === sentenceId; });
                    if (sentence) {
                        self.openSentenceModal(sentence);
                    } else {
                        self.showError('문장 정보를 찾을 수 없습니다.');
                    }
                })
                .catch(function(error) {
                    console.error('문장 정보 로드 실패:', error);
                    self.showError('문장 정보를 불러오는데 실패했습니다.');
                });
        }
    }

    saveSentence() {
        const self = this;

        if (this.isSavingSentence) {
            console.warn('문장 저장이 이미 진행 중입니다.');
            return;
        }

        this.isSavingSentence = true;

        const sentenceId = document.getElementById('sentence-id').value;
        const audioFile = document.getElementById('sentence-audio-file').files[0];

        const sentenceData = {
            english: document.getElementById('sentence-english').value.trim(),
            korean: document.getElementById('sentence-korean').value.trim(),
            level: parseInt(document.getElementById('sentence-level').value),
            day: Number(document.getElementById('sentence-day').value) // day 필드 추가
        };

        // 필수 필드 검증
        if (!sentenceData.english || !sentenceData.korean) {
            this.showError('영어 문장과 한국어 번역을 모두 입력해주세요.');
            this.isSavingSentence = false;
            return;
        }

        const endpoint = sentenceId ? '/sentences/' + sentenceId : '/sentences';
        const method = sentenceId ? 'PUT' : 'POST';

        this.showLoading(sentenceId ? '문장을 수정하는 중...' : '문장을 추가하는 중...');

        this.apiCall(endpoint, method, sentenceData)
            .then(function(savedSentence) {
                // 문장 저장 성공 후 음원 업로드
                if (audioFile && savedSentence.id) {
                    return self.uploadAudioFile(savedSentence.id, audioFile, 'sentence');
                }
                return Promise.resolve();
            })
            .then(function() {
                self.hideLoading();
                self.showSuccess(sentenceId ? '문장이 수정되었습니다.' : '문장이 추가되었습니다.');
                self.closeModal('sentence-modal');
                self.loadSentences();
                self.loadStats();
                self.isSavingSentence = false;
            })
            .catch(function(error) {
                console.error('문장 저장 실패:', error);
                self.hideLoading();
                self.showError(error.message || '문장 저장에 실패했습니다.');
                self.isSavingSentence = false;
            });
    }

    deleteSentence(sentenceId) {
        const self = this;

        if (!confirm('정말로 이 문장을 삭제하시겠습니까?')) {
            return;
        }

        this.showLoading('문장을 삭제하는 중...');

        this.apiCall('/sentences/' + sentenceId, 'DELETE')
            .then(function() {
                self.hideLoading();
                self.showSuccess('문장이 삭제되었습니다.');
                self.loadSentences();
                self.loadStats();
            })
            .catch(function(error) {
                console.error('문장 삭제 실패:', error);
                self.hideLoading();
                self.showError('문장 삭제에 실패했습니다.');
            });
    }

    uploadSentenceAudio(sentenceId) {
        const self = this;

        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'audio/*';

        input.onchange = function(e) {
            const file = e.target.files[0];
            if (!file) return;

            if (file.size > 10 * 1024 * 1024) {
                self.showError('파일 크기는 10MB 이하여야 합니다.');
                return;
            }

            if (!file.type.startsWith('audio/')) {
                self.showError('음성 파일만 업로드할 수 있습니다.');
                return;
            }

            self.showLoading('음성 파일을 업로드하는 중...');

            self.uploadAudioFile(sentenceId, file, 'sentence')
                .then(function(response) {
                    self.hideLoading();
                    self.showSuccess('음성 파일이 업로드되었습니다.');
                    self.loadSentences();
                })
                .catch(function(error) {
                    console.error('음성 파일 업로드 실패:', error);
                    self.hideLoading();
                    self.showError('음성 파일 업로드에 실패했습니다.');
                });
        };

        input.click();
    }

    deleteSentenceAudio(sentenceId) {
        const self = this;

        if (!confirm('이 음원을 삭제하시겠습니까?')) {
            return;
        }

        this.apiCall('/sentences/' + sentenceId + '/audio', 'DELETE')
            .then(function() {
                self.showSuccess('음원이 삭제되었습니다.');
                self.loadSentences();
            })
            .catch(function(error) {
                console.error('음원 삭제 실패:', error);
                self.showError('음원 삭제에 실패했습니다.');
            });
    }

    uploadSentencesFile(file) {
        const self = this;

        if (!file) {
            this.showError('파일을 선택해주세요.');
            return;
        }

        const validExtensions = ['.xlsx', '.xls'];
        const fileName = file.name.toLowerCase();
        const isValidFile = validExtensions.some(function(ext) {
            return fileName.endsWith(ext);
        });

        if (!isValidFile) {
            this.showError('엑셀 파일(.xlsx, .xls)만 업로드할 수 있습니다.');
            return;
        }

        if (file.size > 50 * 1024 * 1024) {
            this.showError('파일 크기는 50MB 이하여야 합니다.');
            return;
        }

        this.showLoading('문장 파일을 업로드하고 처리하는 중...');

        this.uploadFile('/sentences/bulk-upload', file)
            .then(function(response) {
                self.hideLoading();

                const successCount = response.successCount || 0;
                const errorCount = response.errorCount || 0;

                let message = successCount + '개의 문장이 성공적으로 등록되었습니다.';
                if (errorCount > 0) {
                    message += '\n' + errorCount + '개의 문장 등록에 실패했습니다.';
                }

                if (errorCount > 0) {
                    self.showWarning(message);
                } else {
                    self.showSuccess(message);
                }

                self.loadSentences();
                self.loadStats();

                const fileInput = document.getElementById('sentence-file');
                if (fileInput) {
                    fileInput.value = '';
                }
            })
            .catch(function(error) {
                console.error('문장 파일 업로드 실패:', error);
                self.hideLoading();
                self.showError('문장 파일 업로드에 실패했습니다: ' + error.message);

                const fileInput = document.getElementById('sentence-file');
                if (fileInput) {
                    fileInput.value = '';
                }
            });
    }

    // ========== 음원 관리 ==========

    // 일괄 음원 업로드 모달 열기
    openBulkAudioModal(type) {
        const modal = document.getElementById('bulk-audio-modal');
        const title = document.getElementById('bulk-audio-modal-title');
        const typeSelect = document.getElementById('bulk-audio-type');
        const zipFile = document.getElementById('bulk-audio-zip-file');
        const progress = document.getElementById('bulk-upload-progress');
        const results = document.getElementById('bulk-upload-results');

        if (modal && title && typeSelect) {
            title.textContent = type === 'words' ? '단어 음원 일괄 업로드' : '문장 음원 일괄 업로드';
            typeSelect.value = type;
            
            // 초기화
            if (zipFile) zipFile.value = '';
            if (progress) progress.style.display = 'none';
            if (results) results.style.display = 'none';
            
            modal.style.display = 'block';
            console.log('✅ 일괄 음원 업로드 모달이 열렸습니다:', type);
        }
    }

    // 일괄 음원 업로드 시작
    startBulkAudioUpload() {
        const self = this;
        const type = document.getElementById('bulk-audio-type').value;
        const zipFile = document.getElementById('bulk-audio-zip-file').files[0];
        
        if (!zipFile) {
            this.showError('ZIP 파일을 선택해주세요.');
            return;
        }

        // 파일 크기 검증 (100MB)
        if (zipFile.size > 100 * 1024 * 1024) {
            this.showError('ZIP 파일 크기는 100MB 이하여야 합니다.');
            return;
        }

        // 진행 상황 표시
        const progress = document.getElementById('bulk-upload-progress');
        const progressFill = document.getElementById('bulk-progress-fill');
        const progressText = document.getElementById('bulk-progress-text');
        
        if (progress) {
            progress.style.display = 'block';
            if (progressFill) progressFill.style.width = '0%';
            if (progressText) progressText.textContent = '업로드 시작 중...';
        }

        // FormData 생성
        const formData = new FormData();
        formData.append('file', zipFile);
        formData.append('type', type);

        // 업로드 시작
        const endpoint = type === 'words' ? '/words/bulk-audio-upload' : '/sentences/bulk-audio-upload';
        
        fetch(this.baseUrl + endpoint, {
            method: 'POST',
            body: formData
        })
        .then(function(response) {
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            return response.json();
        })
        .then(function(result) {
            // 진행률 완료 표시
            if (progressFill) progressFill.style.width = '100%';
            if (progressText) progressText.textContent = '업로드 완료!';
            
            // 결과 표시
            self.showBulkUploadResults(result);
            
            // 목록 새로고침
            if (type === 'words') {
                self.loadWords();
            } else {
                self.loadSentences();
            }
            
            self.showSuccess('일괄 음원 업로드가 완료되었습니다.');
        })
        .catch(function(error) {
            console.error('일괄 음원 업로드 실패:', error);
            
            if (progressText) progressText.textContent = '업로드 실패';
            self.showError('일괄 음원 업로드에 실패했습니다: ' + error.message);
        });
    }

    // 일괄 업로드 결과 표시
    showBulkUploadResults(result) {
        const results = document.getElementById('bulk-upload-results');
        const successCount = document.getElementById('bulk-success-count');
        const errorCount = document.getElementById('bulk-error-count');
        const resultDetails = document.getElementById('bulk-result-details');

        if (results) {
            results.style.display = 'block';
            
            if (successCount) successCount.textContent = result.successCount || 0;
            if (errorCount) errorCount.textContent = result.errorCount || 0;
            
            if (resultDetails && result.details) {
                let detailsHtml = '';
                
                if (result.successFiles && result.successFiles.length > 0) {
                    detailsHtml += '<h5>성공한 파일:</h5><ul>';
                    result.successFiles.slice(0, 10).forEach(function(file) {
                        detailsHtml += '<li style="color: #28a745;">' + file + '</li>';
                    });
                    if (result.successFiles.length > 10) {
                        detailsHtml += '<li>... 외 ' + (result.successFiles.length - 10) + '개</li>';
                    }
                    detailsHtml += '</ul>';
                }
                
                if (result.errorFiles && result.errorFiles.length > 0) {
                    detailsHtml += '<h5>실패한 파일:</h5><ul>';
                    result.errorFiles.slice(0, 10).forEach(function(error) {
                        detailsHtml += '<li style="color: #dc3545;">' + error.file + ': ' + error.reason + '</li>';
                    });
                    if (result.errorFiles.length > 10) {
                        detailsHtml += '<li>... 외 ' + (result.errorFiles.length - 10) + '개</li>';
                    }
                    detailsHtml += '</ul>';
                }
                
                resultDetails.innerHTML = detailsHtml;
            }
        }
    }

    // 음원 관리 탭 로드
    loadAudioManagement() {
        this.loadAudioList();
    }

    // 음원 목록 로드
    loadAudioList() {
        const self = this;
        
        // 병렬로 단어와 문장 데이터 로드
        Promise.all([
            this.apiCall('/words'),
            this.apiCall('/sentences')
        ])
        .then(function(results) {
            const words = results[0] || [];
            const sentences = results[1] || [];
            
            self.renderAudioList(words, sentences);
        })
        .catch(function(error) {
            console.error('음원 목록 로드 실패:', error);
            self.showError('음원 목록을 불러오는데 실패했습니다.');
        });
    }

    // 음원 목록 렌더링
    renderAudioList(words, sentences) {
        const audioList = document.getElementById('audio-list');
        
        if (!audioList) return;

        const audioItems = [];
        
        // 단어 항목들 추가
        words.forEach(function(word) {
            audioItems.push({
                type: 'word',
                id: word.id,
                text: word.english,
                translation: word.korean,
                level: word.level,
                hasAudio: !!word.audioUrl,
                audioUrl: word.audioUrl
            });
        });
        
        // 문장 항목들 추가
        sentences.forEach(function(sentence) {
            audioItems.push({
                type: 'sentence',
                id: sentence.id,
                text: sentence.english,
                translation: sentence.korean || sentence.translation,
                level: sentence.level,
                hasAudio: !!sentence.audioUrl,
                audioUrl: sentence.audioUrl
            });
        });

        // 필터 적용
        const filteredItems = this.applyAudioFilters(audioItems);
        
        if (filteredItems.length === 0) {
            audioList.innerHTML = '<div class="no-data">조건에 맞는 항목이 없습니다.</div>';
            return;
        }

        audioList.innerHTML = filteredItems.map(function(item) {
            const statusClass = item.hasAudio ? 'has-audio' : 'no-audio';
            const statusText = item.hasAudio ? '음원 있음' : '음원 없음';
            
            return '<div class="audio-item" data-type="' + item.type + '" data-id="' + item.id + '">' +
                '<div class="audio-item-info">' +
                '<div class="audio-item-type">' + (item.type === 'word' ? '단어' : '문장') + '</div>' +
                '<div class="audio-item-text">' + item.text + '</div>' +
                '<div class="audio-item-translation">' + item.translation + '</div>' +
                '<div class="audio-item-meta">' +
                '<span>Level ' + item.level + '</span>' +
                '<span class="audio-status ' + statusClass + '">' + statusText + '</span>' +
                '</div>' +
                '</div>' +
                '<div class="audio-item-actions">' +
                (item.hasAudio ? 
                    '<audio controls style="width: 200px;"><source src="' + item.audioUrl + '"></audio>' : 
                    '<span style="color: #999;">음원 없음</span>'
                ) +
                '<button class="audio-btn" onclick="window.adminDashboard.uploadItemAudio(\'' + item.type + '\', ' + item.id + ')">' +
                (item.hasAudio ? '교체' : '업로드') +
                '</button>' +
                (item.hasAudio ? 
                    '<button class="delete-btn" onclick="window.adminDashboard.deleteItemAudio(\'' + item.type + '\', ' + item.id + ')">삭제</button>' : 
                    ''
                ) +
                '</div>' +
                '</div>';
        }).join('');
    }

    // 음원 필터 적용
    applyAudioFilters(items) {
        const typeFilter = document.getElementById('audio-type-filter');
        const statusFilter = document.getElementById('audio-status-filter');
        const levelFilter = document.getElementById('audio-level-filter');
        
        let filtered = items;
        
        // 타입 필터
        if (typeFilter && typeFilter.value !== 'all') {
            const filterType = typeFilter.value === 'words' ? 'word' : 'sentence';
            filtered = filtered.filter(function(item) {
                return item.type === filterType;
            });
        }
        
        // 상태 필터
        if (statusFilter && statusFilter.value !== 'all') {
            const hasAudio = statusFilter.value === 'has-audio';
            filtered = filtered.filter(function(item) {
                return item.hasAudio === hasAudio;
            });
        }
        
        // 레벨 필터
        if (levelFilter && levelFilter.value !== 'all') {
            const level = parseInt(levelFilter.value);
            filtered = filtered.filter(function(item) {
                return item.level === level;
            });
        }
        
        return filtered;
    }

    // 필터 적용 버튼 클릭 시
    applyAudioFilter() {
        this.loadAudioList();
    }

    // 개별 항목 음원 업로드
    uploadItemAudio(type, itemId) {
        const self = this;
        
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'audio/*';
        
        input.onchange = function(e) {
            const file = e.target.files[0];
            if (!file) return;
            
            // 파일 검증
            if (file.size > 10 * 1024 * 1024) {
                self.showError('파일 크기는 10MB 이하여야 합니다.');
                return;
            }
            
            const validTypes = ['audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/mp3', 'audio/mp4', 'audio/x-m4a'];
            if (!validTypes.includes(file.type)) {
                self.showError('MP3, M4A, WAV, OGG 형식의 음원 파일만 업로드 가능합니다.');
                return;
            }
            
            self.showLoading('음원을 업로드하는 중...');
            
            self.uploadAudioFile(itemId, file, type)
                .then(function() {
                    self.hideLoading();
                    self.showSuccess('음원이 업로드되었습니다.');
                    self.loadAudioList();
                })
                .catch(function(error) {
                    self.hideLoading();
                    self.showError('음원 업로드에 실패했습니다: ' + error.message);
                });
        };
        
        input.click();
    }

    // 개별 항목 음원 삭제
    deleteItemAudio(type, itemId) {
        const self = this;
        
        if (!confirm('이 음원을 삭제하시겠습니까?')) {
            return;
        }
        
        const endpoint = type === 'word' ? 
            '/words/' + itemId + '/audio' : 
            '/sentences/' + itemId + '/audio';
        
        this.apiCall(endpoint, 'DELETE')
            .then(function() {
                self.showSuccess('음원이 삭제되었습니다.');
                self.loadAudioList();
            })
            .catch(function(error) {
                console.error('음원 삭제 실패:', error);
                self.showError('음원 삭제에 실패했습니다.');
            });
    }

    // 음원 통계 표시
    showAudioStats() {
        const self = this;
        
        Promise.all([
            this.apiCall('/words'),
            this.apiCall('/sentences')
        ])
        .then(function(results) {
            const words = results[0] || [];
            const sentences = results[1] || [];
            
            const wordStats = {
                total: words.length,
                withAudio: words.filter(function(w) { return w.audioUrl; }).length
            };
            
            const sentenceStats = {
                total: sentences.length,
                withAudio: sentences.filter(function(s) { return s.audioUrl; }).length
            };
            
            const statsMessage = 
                '📊 음원 통계\n\n' +
                '단어:\n' +
                '  - 전체: ' + wordStats.total + '개\n' +
                '  - 음원 있음: ' + wordStats.withAudio + '개\n' +
                '  - 음원 없음: ' + (wordStats.total - wordStats.withAudio) + '개\n' +
                '  - 완성도: ' + Math.round((wordStats.withAudio / wordStats.total) * 100) + '%\n\n' +
                '문장:\n' +
                '  - 전체: ' + sentenceStats.total + '개\n' +
                '  - 음원 있음: ' + sentenceStats.withAudio + '개\n' +
                '  - 음원 없음: ' + (sentenceStats.total - sentenceStats.withAudio) + '개\n' +
                '  - 완성도: ' + Math.round((sentenceStats.withAudio / sentenceStats.total) * 100) + '%';
            
            alert(statsMessage);
        })
        .catch(function(error) {
            console.error('음원 통계 조회 실패:', error);
            self.showError('음원 통계를 조회하는데 실패했습니다.');
        });
    }

    // 누락된 음원 찾기
    findMissingAudio() {
        const statusFilter = document.getElementById('audio-status-filter');
        if (statusFilter) {
            statusFilter.value = 'no-audio';
            this.loadAudioList();
            this.showSuccess('음원이 없는 항목들을 필터링했습니다.');
        }
    }

    // ========== 학습 설정 관리 ==========

    loadSettings() {
        const self = this;
        this.showLoading('학습 설정을 불러오는 중...');

        this.apiCall('/settings')
            .then(function(settings) {
                console.log('설정 로드 성공:', settings);
                self.renderSettings(settings);
                self.hideLoading();
            })
            .catch(function(error) {
                console.error('설정 로드 실패:', error);
                self.hideLoading();

                const defaultSettings = {
                    audioSpeed: 0.8,
                    voiceSpeed: 1,
                    repeatCount: 3,
                    wordCoin: 1,
                    sentenceCoin: 3,
                    streakBonus: 5,
                    levelUpCoin: 100,
                    maxLevel: 10,
                    dailyWordGoal: 10,
                    dailySentenceGoal: 5
                };

                self.renderSettings(defaultSettings);
                self.showWarning('서버에서 설정을 불러올 수 없어 기본값을 표시합니다.');
            });
    }

    renderSettings(settings) {
        const voiceSpeedEl = document.getElementById('voice-speed');
        if (voiceSpeedEl) {
            voiceSpeedEl.value = settings.audioSpeed || 1;
        }

        const repeatCountEl = document.getElementById('repeat-count');
        if (repeatCountEl) {
            repeatCountEl.value = settings.repeatCount || 3;
        }

        const wordCoinsEl = document.getElementById('word-coins');
        if (wordCoinsEl) {
            wordCoinsEl.value = settings.wordCoin || 1;
        }

        const sentenceCoinsEl = document.getElementById('sentence-coins');
        if (sentenceCoinsEl) {
            sentenceCoinsEl.value = settings.sentenceCoin || 3;
        }

        const streakBonusEl = document.getElementById('streak-bonus');
        if (streakBonusEl) {
            streakBonusEl.value = settings.streakBonus || 5;
        }

        const levelupCoinsEl = document.getElementById('levelup-coins');
        if (levelupCoinsEl) {
            levelupCoinsEl.value = settings.levelUpCoin || 100;
        }

        const maxLevelEl = document.getElementById('max-level');
        if (maxLevelEl) {
            maxLevelEl.value = settings.maxLevel || 10;
        }

        const dailyWordsEl = document.getElementById('daily-words');
        if (dailyWordsEl) {
            dailyWordsEl.value = settings.dailyWordGoal || 10;
        }

        const dailySentencesEl = document.getElementById('daily-sentences');
        if (dailySentencesEl) {
            dailySentencesEl.value = settings.dailySentenceGoal || 5;
        }

        console.log('설정이 렌더링되었습니다:', settings);
    }

    saveSettings() {
        const self = this;

        const settingsData = {
            audioSpeed: parseFloat(document.getElementById('voice-speed').value) || 0.8,
            voiceSpeed: parseInt(document.getElementById('voice-speed').value) || 1,
            repeatCount: parseInt(document.getElementById('repeat-count').value) || 3,
            wordCoin: parseInt(document.getElementById('word-coins').value) || 1,
            sentenceCoin: parseInt(document.getElementById('sentence-coins').value) || 3,
            streakBonus: parseInt(document.getElementById('streak-bonus').value) || 5,
            levelUpCoin: parseInt(document.getElementById('levelup-coins').value) || 100,
            maxLevel: parseInt(document.getElementById('max-level').value) || 10,
            dailyWordGoal: parseInt(document.getElementById('daily-words').value) || 10,
            dailySentenceGoal: parseInt(document.getElementById('daily-sentences').value) || 5
        };

        console.log('저장할 설정 데이터:', settingsData);

        if (!this.validateSettings(settingsData)) {
            return;
        }

        this.showLoading('설정을 저장하는 중...');

        this.apiCall('/settings', 'POST', settingsData)
            .then(function(response) {
                console.log('설정 저장 성공:', response);
                self.hideLoading();
                self.showSuccess('설정이 저장되었습니다.');
            })
            .catch(function(error) {
                console.error('설정 저장 실패:', error);
                self.hideLoading();
                self.showError('설정 저장에 실패했습니다.');
            });
    }

    validateSettings(settings) {
        if (settings.audioSpeed < 0.5 || settings.audioSpeed > 2.0) {
            this.showError('음성 속도는 0.5배에서 2.0배 사이여야 합니다.');
            return false;
        }

        if (settings.repeatCount < 1 || settings.repeatCount > 10) {
            this.showError('반복 횟수는 1회에서 10회 사이여야 합니다.');
            return false;
        }

        if (settings.wordCoin < 0 || settings.sentenceCoin < 0 || settings.streakBonus < 0) {
            this.showError('코인 설정은 0 이상이어야 합니다.');
            return false;
        }

        if (settings.levelUpCoin < 1) {
            this.showError('레벨업 필요 코인은 1 이상이어야 합니다.');
            return false;
        }

        if (settings.maxLevel < 1 || settings.maxLevel > 100) {
            this.showError('최대 레벨은 1에서 100 사이여야 합니다.');
            return false;
        }

        if (settings.dailyWordGoal < 1 || settings.dailySentenceGoal < 1) {
            this.showError('일일 목표는 1 이상이어야 합니다.');
            return false;
        }

        return true;
    }

    resetSettings() {
        const self = this;

        if (!confirm('모든 설정을 기본값으로 초기화하시겠습니까?')) {
            return;
        }

        const defaultSettings = {
            audioSpeed: 0.8,
            voiceSpeed: 1,
            repeatCount: 3,
            wordCoin: 1,
            sentenceCoin: 3,
            streakBonus: 5,
            levelUpCoin: 100,
            maxLevel: 10,
            dailyWordGoal: 10,
            dailySentenceGoal: 5
        };

        this.renderSettings(defaultSettings);
        this.showSuccess('설정이 기본값으로 초기화되었습니다. 저장 버튼을 눌러주세요.');
    }

    exportSettings() {
        const settingsData = {
            audioSpeed: parseFloat(document.getElementById('voice-speed').value),
            voiceSpeed: parseInt(document.getElementById('voice-speed').value),
            repeatCount: parseInt(document.getElementById('repeat-count').value),
            wordCoin: parseInt(document.getElementById('word-coins').value),
            sentenceCoin: parseInt(document.getElementById('sentence-coins').value),
            streakBonus: parseInt(document.getElementById('streak-bonus').value),
            levelUpCoin: parseInt(document.getElementById('levelup-coins').value),
            maxLevel: parseInt(document.getElementById('max-level').value),
            dailyWordGoal: parseInt(document.getElementById('daily-words').value),
            dailySentenceGoal: parseInt(document.getElementById('daily-sentences').value),
            exportDate: new Date().toISOString()
        };

        const dataStr = JSON.stringify(settingsData, null, 2);
        const dataBlob = new Blob([dataStr], { type: 'application/json' });

        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = 'learning_settings_' + new Date().toISOString().split('T')[0] + '.json';
        link.click();

        this.showSuccess('설정이 파일로 내보내졌습니다.');
    }

    importSettings() {
        const self = this;

        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.json';

        input.onchange = function(e) {
            const file = e.target.files[0];
            if (!file) return;

            const reader = new FileReader();
            reader.onload = function(e) {
                try {
                    const settings = JSON.parse(e.target.result);

                    if (self.validateSettings(settings)) {
                        self.renderSettings(settings);
                        self.showSuccess('설정이 가져와졌습니다. 저장 버튼을 눌러주세요.');
                    }
                } catch (error) {
                    console.error('설정 파일 파싱 실패:', error);
                    self.showError('설정 파일을 읽을 수 없습니다.');
                }
            };
            reader.readAsText(file);
        };

        input.click();
    }

    // === 파일명 정규화 버튼 이벤트 핸들러 추가 ===
    bindNormalizeAudioFilenamesEvent() {
        const normalizeBtn = document.getElementById('normalize-audio-filenames-btn');
        if (normalizeBtn) {
            normalizeBtn.addEventListener('click', () => {
                if (confirm('기존 DB의 오디오 파일명을 정규화하시겠습니까?\n\n이 작업은 다음과 같이 변환합니다:\n• "문장 3번, ..." → "sentence3.wav"\n• "14번 ..." → "no14.wav"\n\n⚠️ 주의: 실제 파일명도 함께 변경해야 합니다.')) {
                    this.showLoading('오디오 파일명을 정규화하고 있습니다...');
                    
                    fetch(this.baseUrl + '/normalize-audio-filenames', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        }
                    })
                    .then(response => response.json())
                    .then(result => {
                        this.hideLoading();
                        if (result.success) {
                            this.showSuccess(result.message);
                            // 현재 탭 데이터 새로고침
                            if (this.currentTab === 'words') {
                                this.loadWords();
                            } else if (this.currentTab === 'sentences') {
                                this.loadSentences();
                            }
                        } else {
                            this.showError(result.message);
                        }
                    })
                    .catch(error => {
                        this.hideLoading();
                        console.error('파일명 정규화 실패:', error);
                        this.showError('파일명 정규화 중 오류가 발생했습니다.');
                    });
                }
            });
        }
    }

    // ========== 유틸리티 메서드 ==========

    closeModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
        }
    }

    showSuccess(message) {
        if (window.kiribocaApp && window.kiribocaApp.showToast) {
            window.kiribocaApp.showToast('성공', message);
        } else {
            alert('성공: ' + message);
        }
    }

    showError(message) {
        if (window.kiribocaApp && window.kiribocaApp.showToast) {
            window.kiribocaApp.showToast('오류', message);
        } else {
            alert('오류: ' + message);
        }
    }

    showWarning(message) {
        if (window.kiribocaApp && window.kiribocaApp.showToast) {
            window.kiribocaApp.showToast('경고', message);
        } else {
            alert('경고: ' + message);
        }
    }

    showLoading(message) {
        console.log('Loading:', message);

        let loadingEl = document.getElementById('admin-loading');
        if (!loadingEl) {
            loadingEl = document.createElement('div');
            loadingEl.id = 'admin-loading';
            loadingEl.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0,0,0,0.5);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 10000;
                color: white;
                font-size: 16px;
            `;
            document.body.appendChild(loadingEl);
        }
        loadingEl.textContent = message || '처리 중...';
        loadingEl.style.display = 'flex';
    }

    hideLoading() {
        console.log('Loading finished');
        const loadingEl = document.getElementById('admin-loading');
        if (loadingEl) {
            loadingEl.style.display = 'none';
        }
    }

    // 체크박스 이벤트 바인딩
    bindWordCheckboxEvents() {
        const self = this;
        const checkboxes = document.querySelectorAll('.word-checkbox');
        
        checkboxes.forEach(function(checkbox) {
            checkbox.addEventListener('change', function() {
                self.updateWordDeleteButton();
                self.updateSelectAllWordCheckbox();
            });
        });
    }

    bindSelectAllWordEvents() {
        const self = this;
        const selectAllCheckbox = document.getElementById('select-all-words');
        
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', function() {
                const isChecked = this.checked;
                const wordCheckboxes = document.querySelectorAll('.word-checkbox');
                
                wordCheckboxes.forEach(function(checkbox) {
                    checkbox.checked = isChecked;
                });
                
                self.updateWordDeleteButton();
            });
        }
    }

    bindSentenceCheckboxEvents() {
        const self = this;
        const checkboxes = document.querySelectorAll('.sentence-checkbox');
        
        checkboxes.forEach(function(checkbox) {
            checkbox.addEventListener('change', function() {
                self.updateSentenceDeleteButton();
                self.updateSelectAllSentenceCheckbox();
            });
        });
    }

    bindSelectAllSentenceEvents() {
        const self = this;
        const selectAllCheckbox = document.getElementById('select-all-sentences');
        
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', function() {
                const isChecked = this.checked;
                const sentenceCheckboxes = document.querySelectorAll('.sentence-checkbox');
                
                sentenceCheckboxes.forEach(function(checkbox) {
                    checkbox.checked = isChecked;
                });
                
                self.updateSentenceDeleteButton();
            });
        }
    }

    // 삭제 버튼 상태 업데이트
    updateWordDeleteButton() {
        const checkedBoxes = document.querySelectorAll('.word-checkbox:checked');
        const deleteBtn = document.getElementById('delete-selected-words-btn');
        
        if (deleteBtn) {
            if (checkedBoxes.length > 0) {
                deleteBtn.style.display = 'inline-block';
                deleteBtn.textContent = `🗑️ 선택 삭제 (${checkedBoxes.length}개)`;
            } else {
                deleteBtn.style.display = 'none';
            }
        }
    }

    updateSentenceDeleteButton() {
        const checkedBoxes = document.querySelectorAll('.sentence-checkbox:checked');
        const deleteBtn = document.getElementById('delete-selected-sentences-btn');
        
        if (deleteBtn) {
            if (checkedBoxes.length > 0) {
                deleteBtn.style.display = 'inline-block';
                deleteBtn.textContent = `🗑️ 선택 삭제 (${checkedBoxes.length}개)`;
            } else {
                deleteBtn.style.display = 'none';
            }
        }
    }

    // 전체선택 체크박스 상태 업데이트
    updateSelectAllWordCheckbox() {
        const selectAllCheckbox = document.getElementById('select-all-words');
        const wordCheckboxes = document.querySelectorAll('.word-checkbox');
        const checkedBoxes = document.querySelectorAll('.word-checkbox:checked');
        
        if (selectAllCheckbox && wordCheckboxes.length > 0) {
            if (checkedBoxes.length === 0) {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = false;
            } else if (checkedBoxes.length === wordCheckboxes.length) {
                selectAllCheckbox.checked = true;
                selectAllCheckbox.indeterminate = false;
            } else {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = true;
            }
        }
    }

    updateSelectAllSentenceCheckbox() {
        const selectAllCheckbox = document.getElementById('select-all-sentences');
        const sentenceCheckboxes = document.querySelectorAll('.sentence-checkbox');
        const checkedBoxes = document.querySelectorAll('.sentence-checkbox:checked');
        
        if (selectAllCheckbox && sentenceCheckboxes.length > 0) {
            if (checkedBoxes.length === 0) {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = false;
            } else if (checkedBoxes.length === sentenceCheckboxes.length) {
                selectAllCheckbox.checked = true;
                selectAllCheckbox.indeterminate = false;
            } else {
                selectAllCheckbox.checked = false;
                selectAllCheckbox.indeterminate = true;
            }
        }
    }

    // 선택된 단어 삭제
    deleteSelectedWords() {
        const self = this;
        const checkedBoxes = document.querySelectorAll('.word-checkbox:checked');
        
        if (checkedBoxes.length === 0) {
            this.showWarning('삭제할 단어를 선택해주세요.');
            return;
        }

        const selectedIds = Array.from(checkedBoxes).map(function(checkbox) {
            return parseInt(checkbox.getAttribute('data-word-id'));
        });

        const confirmMessage = `선택된 ${selectedIds.length}개의 단어를 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`;
        
        if (!confirm(confirmMessage)) {
            return;
        }

        this.showLoading(`${selectedIds.length}개의 단어를 삭제하는 중...`);

        // 순차적으로 삭제
        const deletePromises = selectedIds.map(function(wordId) {
            return self.apiCall('/words/' + wordId, 'DELETE');
        });

        Promise.all(deletePromises)
            .then(function() {
                self.hideLoading();
                self.showSuccess(`${selectedIds.length}개의 단어가 삭제되었습니다.`);
                self.loadWords();
                self.loadStats();
            })
            .catch(function(error) {
                console.error('단어 삭제 실패:', error);
                self.hideLoading();
                self.showError('일부 단어 삭제에 실패했습니다.');
                self.loadWords();
                self.loadStats();
            });
    }

    // 선택된 문장 삭제
    deleteSelectedSentences() {
        const self = this;
        const checkedBoxes = document.querySelectorAll('.sentence-checkbox:checked');
        
        if (checkedBoxes.length === 0) {
            this.showWarning('삭제할 문장을 선택해주세요.');
            return;
        }

        const selectedIds = Array.from(checkedBoxes).map(function(checkbox) {
            return parseInt(checkbox.getAttribute('data-sentence-id'));
        });

        const confirmMessage = `선택된 ${selectedIds.length}개의 문장을 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`;
        
        if (!confirm(confirmMessage)) {
            return;
        }

        this.showLoading(`${selectedIds.length}개의 문장을 삭제하는 중...`);

        // 순차적으로 삭제
        const deletePromises = selectedIds.map(function(sentenceId) {
            return self.apiCall('/sentences/' + sentenceId, 'DELETE');
        });

        Promise.all(deletePromises)
            .then(function() {
                self.hideLoading();
                self.showSuccess(`${selectedIds.length}개의 문장이 삭제되었습니다.`);
                self.loadSentences();
                self.loadStats();
            })
            .catch(function(error) {
                console.error('문장 삭제 실패:', error);
                self.hideLoading();
                self.showError('일부 문장 삭제에 실패했습니다.');
                self.loadSentences();
                self.loadStats();
            });
    }
}

// 전역 접근을 위한 변수
window.adminDashboard = null;

// AdminDashboard 초기화 함수 (메인 앱에서 호출)
window.initAdminDashboard = function() {
    console.log('🔧 AdminDashboard 초기화 함수 호출됨');

    // 기존 인스턴스가 있다면 제거
    if (window.adminDashboard) {
        window.adminDashboard = null;
    }

    // 새 인스턴스 생성
    window.adminDashboard = new AdminDashboard();

    return window.adminDashboard;
};