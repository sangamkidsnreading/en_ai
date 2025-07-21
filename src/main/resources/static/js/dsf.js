// 결제 페이지에 정보 수정 폼을 자동으로 삽입하는 스크립트
(function() {
    // 현재 URL이 결제 페이지인지 확인
    function isPaymentPage() {
        const currentUrl = window.location.href;
        return currentUrl.includes('shop_payment/?order_code=');
    }
    
    // 대상 상품인지 확인 (스타터, 트렌드세터, 언리미티드 CODI PACK)
    function isTargetProduct() {
        const targetProductNames = [
            '스타터 CODI PACK',
            '트렌드세터 CODI PACK', 
            '언리미티드 CODI PACK'
        ];
        
        console.log('상품 확인을 시작합니다...');
        
        // 상품명으로 확인
        const productNameElements = document.querySelectorAll('.css-a0a2v3');
        console.log('찾은 상품명 요소 개수:', productNameElements.length);
        
        for (let i = 0; i < productNameElements.length; i++) {
            const element = productNameElements[i];
            const productName = element.textContent.trim();
            console.log(`상품 ${i + 1}:`, productName);
            
            if (targetProductNames.includes(productName)) {
                console.log('✅ 대상 상품 발견:', productName);
                return true;
            }
        }
        
        // 추가로 다른 셀렉터로도 시도
        const alternativeSelectors = [
            'p:contains("CODI PACK")',
            '.css-1kuy7z7 p:first-child',
            '[class*="css"] p:contains("CODI")'
        ];
        
        for (let selector of alternativeSelectors) {
            try {
                const elements = document.querySelectorAll(selector);
                console.log(`추가 셀렉터 ${selector} 결과:`, elements.length);
                for (let element of elements) {
                    const text = element.textContent.trim();
                    console.log('추가 검색 텍스트:', text);
                    if (targetProductNames.includes(text)) {
                        console.log('✅ 추가 검색에서 대상 상품 발견:', text);
                        return true;
                    }
                }
            } catch (e) {
                console.log('셀렉터 오류:', selector, e);
            }
        }
        
        // 전체 페이지에서 텍스트 검색
        const bodyText = document.body.textContent;
        for (let productName of targetProductNames) {
            if (bodyText.includes(productName)) {
                console.log('✅ 페이지 전체 텍스트에서 대상 상품 발견:', productName);
                return true;
            }
        }
        
        console.log('❌ 대상 상품이 아닙니다.');
        return false;
    }
    
    // 대상 상품인지 확인 (스타터, 트렌드세터, 언리미티드 CODI PACK)
    function isTargetProduct() {
        const targetProductNames = [
            '스타터 CODI PACK',
            '트렌드세터 CODI PACK', 
            '언리미티드 CODI PACK'
        ];
        
        const targetProductIds = [
            'shop_view/?idx=180',  // 스타터 CODI PACK
            'shop_view/?idx=181',  // 트렌드세터 CODI PACK
            'shop_view/?idx=188'   // 언리미티드 CODI PACK
        ];
        
        // 상품명으로 확인
        const productNameElements = document.querySelectorAll('.css-a0a2v3');
        for (let element of productNameElements) {
            const productName = element.textContent.trim();
            if (targetProductNames.includes(productName)) {
                console.log('대상 상품 발견 (상품명):', productName);
                return true;
            }
        }
        
        // 상품 링크로 확인
        const productLinks = document.querySelectorAll('a[href*="shop_view"]');
        for (let link of productLinks) {
            const href = link.getAttribute('href');
            for (let targetId of targetProductIds) {
                if (href && href.includes(targetId)) {
                    console.log('대상 상품 발견 (링크):', href);
                    return true;
                }
            }
        }
        
        console.log('대상 상품이 아닙니다.');
        return false;
    }
    
    // 페이지가 완전히 로드된 후 실행
    function injectForm() {
        console.log('🔍 injectForm 함수 시작');
        console.log('현재 URL:', window.location.href);
        
        // 결제 페이지가 아니면 실행하지 않음
        if (!isPaymentPage()) {
            console.log('❌ 결제 페이지가 아닙니다. 스크립트를 실행하지 않습니다.');
            return;
        }
        console.log('✅ 결제 페이지 확인됨');
        
        // 대상 상품이 아니면 실행하지 않음
        if (!isTargetProduct()) {
            console.log('❌ 대상 상품이 아닙니다. 스크립트를 실행하지 않습니다.');
            return;
        }
        console.log('✅ 대상 상품 확인됨');
        
        console.log('🚀 결제 페이지에서 대상 상품 확인됨. 폼 삽입을 시작합니다.');
        
        // 타겟 위치 찾기: "주문 상품 정보" 다음
        console.log('🔍 타겟 위치를 찾는 중...');
        const targetElements = document.querySelectorAll('.css-17g8nhj');
        console.log('css-17g8nhj 요소 개수:', targetElements.length);
        
        let targetContainer = null;
        
        // "주문 상품 정보" 헤더를 찾기
        for (let i = 0; i < targetElements.length; i++) {
            const element = targetElements[i];
            const text = element.textContent.trim();
            console.log(`헤더 ${i + 1}:`, text);
            
            if (text === '주문 상품 정보') {
                targetContainer = element.closest('.css-2nfxqo');
                console.log('✅ 주문 상품 정보 헤더를 찾았습니다!');
                break;
            }
        }
        
        if (targetContainer) {
            console.log('✅ 주문 상품 정보 섹션을 찾았습니다.');
            
            // 이미 폼이 삽입되었는지 확인
            if (document.getElementById('user_info_edit_form')) {
                console.log('⚠️ 폼이 이미 존재합니다.');
                return;
            }
            
            // 새로운 섹션 생성
            const formSection = document.createElement('div');
            formSection.className = 'css-2nfxqo';
            formSection.id = 'user_info_edit_section';
            
            formSection.innerHTML = `
                <div class="css-1ojf7x">
                    <header class="css-17g8nhj">회원 정보 수정</header>
                    <div id="user_info_edit_form" style="padding: 20px; background: #f9f9f9; border-radius: 8px; margin: 10px 0;">
                        <div class="loading" style="text-align: center; padding: 30px; color: #666;">
                            <div style="border: 3px solid #f3f3f3; border-top: 3px solid #007bff; border-radius: 50%; width: 30px; height: 30px; animation: spin 1s linear infinite; margin: 0 auto 20px;"></div>
                            <p>회원 정보를 불러오는 중...</p>
                        </div>
                    </div>
                </div>
                <div></div>
            `;
            
            // CSS 애니메이션 추가
            const style = document.createElement('style');
            style.textContent = `
                @keyframes spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }
                
                .form-group {
                    margin-bottom: 15px;
                }
                
                .form-group label {
                    display: block;
                    margin-bottom: 5px;
                    font-weight: 500;
                    color: #333;
                }
                
                .form-group input, .form-group textarea {
                    width: 100%;
                    padding: 10px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    font-size: 14px;
                    box-sizing: border-box;
                }
                
                .btn-primary {
                    background: #007bff;
                    color: white;
                    padding: 12px 24px;
                    border: none;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 16px;
                    font-weight: 500;
                    width: 100%;
                    height: 50px;
                    transition: background-color 0.2s;
                }
                
                .btn-primary:hover {
                    background: #0056b3;
                }
                
                .required {
                    color: #dd3344;
                    margin-left: 3px;
                }
                
                .profile-img {
                    width: 80px;
                    height: 80px;
                    border-radius: 50%;
                    margin: 0 auto 20px;
                    background-size: cover;
                    background-position: center;
                    border: 1px solid #ddd;
                }
                
                .input_block {
                    margin-bottom: 15px;
                }
                
                .mini-tit {
                    display: block;
                    margin-bottom: 5px;
                    font-weight: 500;
                    color: #333;
                }
                
                .input_form input {
                    width: 100%;
                    padding: 10px;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    font-size: 14px;
                    box-sizing: border-box;
                }
                
                .alert-block {
                    color: #dc3545;
                    font-size: 12px;
                    margin-top: 5px;
                }
                
                .icon-required {
                    color: #dd3344;
                    margin-left: 3px;
                }
            `;
            document.head.appendChild(style);
            
            // "주문 상품 정보" 섹션 다음에 삽입
            targetContainer.insertAdjacentElement('afterend', formSection);
            
            console.log('✅ 폼이 성공적으로 삽입되었습니다.');
            
            // 폼 스크립트 로드
            setTimeout(loadFormScript, 100);
        } else {
            console.log('❌ 주문 상품 정보 섹션을 찾을 수 없습니다.');
            console.log('페이지의 모든 헤더 요소들:');
            const allHeaders = document.querySelectorAll('header, h1, h2, h3, h4, h5, h6, [class*="header"], [class*="title"]');
            allHeaders.forEach((header, index) => {
                console.log(`헤더 ${index + 1}: "${header.textContent.trim()}" (클래스: ${header.className})`);
            });
            
            console.log('2초 후 재시도...');
            setTimeout(function() {
                // 재시도 시에도 상품 확인
                if (isTargetProduct()) {
                    injectForm();
                } else {
                    console.log('재시도 시 대상 상품이 아님을 확인했습니다.');
                }
            }, 2000);
        }
    }
    
    // 폼 관련 스크립트 로드
    function loadFormScript() {
        // jQuery가 이미 로드되어 있는지 확인
        if (typeof jQuery !== 'undefined') {
            loadUserDataAndCreateForm();
        } else {
            console.error('jQuery가 로드되지 않았습니다.');
            createBasicForm();
        }
    }
    
    // 사용자 데이터 로드 후 폼 생성
    function loadUserDataAndCreateForm() {
        console.log('사용자 데이터 로드 시작');
        
        $.ajax({
            type: 'GET',
            url: '/dialog/join.cm',
            data: {
                mode: 'edit',
                is_popup: 'Y'
            },
            dataType: 'html',
            cache: false,
            success: function(html) {
                console.log("사용자 데이터 로드 성공");
                
                const $serverForm = $(html);
                const userData = {
                    email: $serverForm.find('input[name="email"]').val() || '',
                    name: $serverForm.find('input[name="name"]').val() || '',
                    callnum: $serverForm.find('input[name="callnum"]').val() || '',
                    birth: $serverForm.find('input[name="join_form_30772"]').val() || '',
                    height: $serverForm.find('input[name="join_form_30773"]').val() || '',
                    weight: $serverForm.find('input[name="join_form_30774"]').val() || '',
                    topSize: $serverForm.find('input[name="join_form_30775"]').val() || '',
                    bottomSize: $serverForm.find('input[name="join_form_30776"]').val() || '',
                    style: $serverForm.find('input[name="join_form_30777"]').val() || '',
                    dislike: $serverForm.find('input[name="join_form_30778"]').val() || '',
                    body: $serverForm.find('input[name="join_form_30780"]').val() || '',
                    request: $serverForm.find('input[name="join_form_30781"]').val() || '',
                    uid: $serverForm.find('input[name="uid"]').val() || '',
                    token: $serverForm.find('input[name="join_token"]').val() || '',
                    tokenKey: $serverForm.find('input[name="join_token_key"]').val() || '',
                    idx: $serverForm.find('input[name="idx"]').val() || '',
                    code: $serverForm.find('input[name="code"]').val() || '',
                    typeCode: $serverForm.find('input[name="type_code"]').val() || '',
                    photo: $serverForm.find('input[name="photo"]').val() || '/common/img/default_profile.png'
                };
                
                createFormHTML(userData);
            },
            error: function(xhr, status, error) {
                console.error('사용자 데이터 로드 실패:', error);
                createBasicForm();
            }
        });
    }
    
    // 실제 폼 HTML 생성
    function createFormHTML(userData) {
        const formContainer = document.getElementById('user_info_edit_form');
        
        formContainer.innerHTML = `
            <form id="join_form" method="post" action="/backpg/join.cm" class="form form-validate" role="form" target="hidden_frame" enctype="multipart/form-data" style="display: block;">
                <input type="hidden" name="limit_join_agree" id="limit_join_agree" value="Y">
                <input type="hidden" name="idx" value="${userData.idx}">
                <input type="hidden" name="code" value="${userData.code}">
                <input type="hidden" name="type_code" value="${userData.typeCode || 't202301116c092cf6525c0'}">
                <input type="hidden" name="back_url" value="L3Nob3BfbXlwYWdl">
                <input type="hidden" name="is_popup" value="Y">
                <input type="hidden" name="mode" value="edit">
                <input type="hidden" name="photo_tmp_idx" id="photo_tmp_idx2" value="">
                <input type="hidden" name="photo" id="photo" value="${userData.photo}">
                    
                <div class="_token_obj" data-type="join" id="join_token">
                    <input type="hidden" name="join_token" class="_tk_obj" value="${userData.token}">
                    <input type="hidden" name="join_token_key" class="_tk_key_obj" value="${userData.tokenKey}">
                </div>

                <div style="text-align: center; margin-bottom: 20px;">
                    <div class="profile-img" style="background-image: url('${userData.photo}');"></div>
                </div>

                <div style="position: relative">
                    <div style="margin-bottom: 15px; display: none;">
                        <div style="position: relative; margin-bottom: 10px;">
                            <input title="아이디" type="text" name="uid" readonly="readonly" value="${userData.uid}" placeholder="아이디" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; background-color: #f5f5f5;">
                            <div class="alert-block _msg"></div>
                        </div>
                        <div style="position: relative; margin-bottom: 10px;">
                            <input title="기본 비밀번호 입력" type="password" name="passwd_org" autocomplete="off" placeholder="기본 비밀번호 입력" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                        <div style="position: relative; margin-bottom: 10px;">
                            <input title="비밀번호" type="password" name="passwd" autocomplete="off" placeholder="비밀번호를 변경 하는 경우 입력하세요" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block first-letter-uppercase _msg"></div>
                        </div>
                        <div style="position: relative; margin-bottom: 10px;">
                            <input title="비밀번호 확인" type="password" name="passwd_confirm" autocomplete="off" placeholder="비밀번호 확인" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_email" style="display: block; margin-bottom: 5px; font-weight: 500;">이메일<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <div style="position: relative;">
                            <input type="email" id="join_email" name="email" placeholder="이메일" value="${userData.email}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_name" style="display: block; margin-bottom: 5px; font-weight: 500;">이름<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <div style="position: relative;">
                            <input title="이름을(를) 입력하세요" type="text" id="join_name" name="name" placeholder="이름을(를) 입력하세요" value="${userData.name}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_callnum" style="display: block; margin-bottom: 5px; font-weight: 500;">연락처</label>
                        <div style="position: relative;">
                            <input type="tel" id="join_callnum" name="callnum" placeholder="연락처" value="${userData.callnum}" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_30772" style="display: block; margin-bottom: 5px; font-weight: 500;">나이가 어떻게 되시나요? (출생연도)<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <input type="hidden" name="join_form[]" value="30772">
                        <div style="position: relative;">
                            <input type="text" id="join_30772" name="join_form_30772" placeholder="ex) 1997" value="${userData.birth}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px;">
                        <label for="join_30773" style="display: block; margin-bottom: 5px; font-weight: 500;">키를 입력해주세요 (cm)<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <input type="hidden" name="join_form[]" value="30773">
                        <div style="position: relative;">
                            <input type="text" id="join_30773" name="join_form_30773" placeholder="" value="${userData.height}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px;">
                        <label for="join_30774" style="display: block; margin-bottom: 5px; font-weight: 500;">몸무게를 입력해주세요 (kg)<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <input type="hidden" name="join_form[]" value="30774">
                        <div style="position: relative;">
                            <input type="text" id="join_30774" name="join_form_30774" placeholder="" value="${userData.weight}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px;">
                        <label for="join_30775" style="display: block; margin-bottom: 5px; font-weight: 500;">평소 입는 상의 사이즈를 기재해주세요.<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <input type="hidden" name="join_form[]" value="30775">
                        <div style="position: relative;">
                            <input type="text" id="join_30775" name="join_form_30775" placeholder="ex) 95, 100, 105 등" value="${userData.topSize}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px;">
                        <label for="join_30776" style="display: block; margin-bottom: 5px; font-weight: 500;">평소 입는 하의 사이즈를 기재해주세요.<span style="color: #dd3344; margin-left: 3px;">*</span></label>
                        <input type="hidden" name="join_form[]" value="30776">
                        <div style="position: relative;">
                            <input type="text" id="join_30776" name="join_form_30776" placeholder="ex) 28, 30, 32 등" value="${userData.bottomSize}" required="required" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_30777" style="display: block; margin-bottom: 5px; font-weight: 500;">좋아하는 스타일 또는 평소 자주 입는 스타일이 있다면 기재해주세요 [선택]</label>
                        <input type="hidden" name="join_form[]" value="30777">
                        <div style="position: relative;">
                            <input type="text" id="join_30777" name="join_form_30777" placeholder="ex) 평소 캐주얼하게 입어요, 깔끔하게 입어요" value="${userData.style}" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_30778" style="display: block; margin-bottom: 5px; font-weight: 500;">받아보고 싶지 않은 옷들(옷 종류, 컬러 등)이 있으신 경우 기재해주세요. [선택]</label>
                        <input type="hidden" name="join_form[]" value="30778">
                        <div style="position: relative;">
                            <input type="text" id="join_30778" name="join_form_30778" placeholder="ex) 검정 슬랙스는 있어요, 반바지 싫어요" value="${userData.dislike}" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_30780" style="display: block; margin-bottom: 5px; font-weight: 500;">신체적 특성이 있으신 경우 기재해주세요 [선택]</label>
                        <input type="hidden" name="join_form[]" value="30780">
                        <div style="position: relative;">
                            <input type="text" id="join_30780" name="join_form_30780" placeholder="ex) 엉덩이가 커요, 허벅지가 두꺼워요." value="${userData.body}" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin-bottom: 15px; display: none;">
                        <label for="join_30781" style="display: block; margin-bottom: 5px; font-weight: 500;">기타 스타일링 시 요청사항이 있다면 작성해주세요 [선택]</label>
                        <input type="hidden" name="join_form[]" value="30781">
                        <div style="position: relative;">
                            <input type="text" id="join_30781" name="join_form_30781" placeholder="ex) 소개팅에 입고 나갈 옷이 필요해요." value="${userData.request}" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px;">
                            <div class="alert-block _msg"></div>
                        </div>
                    </div>

                    <div style="margin: 0;">
                        <div style="text-align: center; margin: 0;">
                            <button type="button" class="btn btn-brand w100p h45 _join_btn" style="display: inline-block; padding: 12px 24px; margin: 0; font-size: 16px; font-weight: 500; text-align: center; cursor: pointer; border: none; border-radius: 6px; color: #fff; background-color: #007bff; width: 100%; height: 50px; transition: background-color 0.2s;">정보 수정하기</button>
                        </div>
                    </div>
                </div>
            </form>
        `;
        
        // 폼 기능 초기화
        initFormFunctions();
    }
    
    // 기본 폼 생성 (데이터 로드 실패시)
    function createBasicForm() {
        const formContainer = document.getElementById('user_info_edit_form');
        formContainer.innerHTML = `
            <div style="text-align: center; padding: 30px; color: #dc3545;">
                <h3>오류</h3>
                <p>회원 정보를 불러올 수 없습니다.</p>
                <button onclick="location.reload()" style="padding: 10px 20px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                    페이지 새로고침
                </button>
            </div>
        `;
    }
    
    // 폼 기능 초기화
    function initFormFunctions() {
        // jQuery serializeObject 플러그인
        $.fn.serializeObject = function() {
            var o = {};
            var a = this.serializeArray();
            $.each(a, function() {
                if (o[this.name] !== undefined) {
                    if (!o[this.name].push) {
                        o[this.name] = [o[this.name]];
                    }
                    o[this.name].push(this.value || '');
                } else {
                    o[this.name] = this.value || '';
                }
            });
            return o;
        };

        // 전역 변수
        var join_processing = false;
        var $join_form = $('#join_form');

        // 폼 제출 함수
        var joinSubmit = function(is_social){
            console.log("joinSubmit 함수 시작");
            
            if(join_processing)
                return false;
            join_processing = true;
            
            // 토큰 갱신 후 폼 제출
            refreshTokenAndSubmit(is_social);
        };

        // 토큰 갱신 후 폼 제출하는 함수
        function refreshTokenAndSubmit(is_social) {
            console.log("토큰 갱신 중...");
            
            // 아임웹의 TOKEN 함수 사용
            if (typeof TOKEN !== 'undefined') {
                TOKEN.makeToken(3600, 'join_popup', function(token, token_key) {
                    console.log("새 토큰 받아옴:", token_key);
                    
                    // 토큰 업데이트
                    $('._tk_obj').val(token);
                    $('._tk_key_obj').val(token_key);
                    
                    console.log("토큰 업데이트 완료, 폼 제출 시작");
                    
                    // 실제 폼 제출
                    submitFormWithToken(is_social);
                });
            } else {
                // TOKEN 함수가 없는 경우 기존 방식 사용
                console.log("TOKEN 함수를 찾을 수 없어 기존 방식 사용");
                
                $.ajax({
                    type: 'GET',
                    url: '/dialog/join.cm',
                    data: {
                        mode: 'edit',
                        is_popup: 'Y'
                    },
                    dataType: 'html',
                    cache: false,
                    success: function(html) {
                        console.log("새 토큰 받아옴 (기존방식)");
                        
                        var $serverForm = $(html);
                        var token = $serverForm.find('input[name="join_token"]').val();
                        var tokenKey = $serverForm.find('input[name="join_token_key"]').val();
                        
                        // 토큰 업데이트
                        if(token) $('._tk_obj').val(token);
                        if(tokenKey) $('._tk_key_obj').val(tokenKey);
                        
                        console.log("토큰 업데이트 완료, 폼 제출 시작");
                        
                        // 실제 폼 제출
                        submitFormWithToken(is_social);
                    },
                    error: function(xhr, status, error) {
                        console.error('토큰 갱신 실패:', error);
                        alert('토큰 갱신에 실패했습니다. 페이지를 새로고침 후 다시 시도해주세요.');
                        join_processing = false;
                    }
                });
            }
        }

        // 실제 폼 제출 함수
        function submitFormWithToken(is_social) {
            var data = $join_form.serializeObject();
            
            console.log("폼 데이터:", data);
            
            data.force_update = 'Y';
            data.is_social = is_social ? 'Y' : 'N';
            
            console.log("최종 전송 데이터:", data);
            
            $.ajax({
                type:'POST',
                data:data,
                url:('/backpg/join.cm'),
                dataType:'text',
                cache:false,
                success:function(response, textStatus, xhr){
                    console.log("서버 원본 응답:", response);
                    console.log("HTTP 상태 코드:", xhr.status);
                    
                    // HTTP 200 응답이고 빈 응답인 경우 성공으로 처리
                    if (xhr.status === 200 && (!response || response.trim() === '')) {
                        console.log("HTTP 200 + 빈 응답 = 성공으로 처리");
                        alert('정보가 성공적으로 수정되었습니다.');
                        join_processing = false;
                        return;
                    }
                    
                    // 응답이 비어있는 경우
                    if (!response || response.trim() === '') {
                        console.log("서버에서 빈 응답을 보냈습니다.");
                        // 상태 코드가 200이면 성공으로 간주
                        if (xhr.status === 200) {
                            alert('정보가 성공적으로 수정되었습니다.');
                        } else {
                            alert('서버 응답이 비어있습니다. 다시 시도해주세요.');
                        }
                        join_processing = false;
                        return;
                    }
                    
                    // JSON 파싱 시도
                    let res;
                    try {
                        res = JSON.parse(response);
                        console.log("파싱된 서버 응답:", res);
                    } catch (e) {
                        console.error("JSON 파싱 실패:", e);
                        console.log("응답 내용:", response);
                        
                        // HTML 응답인 경우 (성공적인 처리 후 리다이렉트 페이지)
                        if (response.includes('<!DOCTYPE') || response.includes('<html')) {
                            console.log("HTML 응답 감지 - 성공으로 간주");
                            alert('정보가 성공적으로 수정되었습니다.');
                            join_processing = false;
                            return;
                        }
                        
                        // HTTP 200이면 성공으로 간주
                        if (xhr.status === 200) {
                            console.log("HTTP 200 응답 - 성공으로 간주");
                            alert('정보가 성공적으로 수정되었습니다.');
                        } else {
                            alert('서버 응답 형식이 올바르지 않습니다. 다시 시도해주세요.');
                        }
                        join_processing = false;
                        return;
                    }
                    
                    // 정상적인 JSON 응답 처리
                    if(res.msg === 'SUCCESS'){
                        console.log("성공!");
                        alert('정보가 성공적으로 수정되었습니다.');
                        console.log("정보 수정 완료 - 페이지 이동하지 않음");
                        
                    }else{
                        console.log("서버 에러:", res);
                        if(res.code > 0){
                            alert(res.msg);
                            if(res.code === 4){
                                window.location.href = '/';
                            }
                        }else{
                            var $obj = $join_form.find('._' + res.flag);
                            $join_form.find('._item').removeClass('active triangle');
                            
                            $.each(res.accept || [], function(e, v){
                                var $acc_obj = $join_form.find('._' + v);
                                $acc_obj.addClass('active');
                            });

                            $obj.addClass('triangle');
                            $obj.find('input').focus();
                            if ($obj[0]) $obj[0].scrollIntoView({ behavior: 'smooth', block: 'center' });

                            const doubleCheck = $obj.find('._msg').text() === res.msg;
                            if (!doubleCheck) $obj.find('._msg').text(res.msg);

                            $join_form.find('._' + res.flag).removeClass('active');
                            if(res.flag === 'passwd') $('._passwd_confirm').removeClass('active');
                        }
                    }
                    join_processing = false;
                },
                error: function(xhr, status, error) {
                    console.error('Ajax error:', xhr, status, error);
                    console.log("응답 텍스트:", xhr.responseText);
                    alert('오류가 발생했습니다. 다시 시도해주세요.');
                    join_processing = false;
                }
            });
        }

        // 이벤트 바인딩
        console.log("이벤트 바인딩 시작");
        
        // 폼 제출 이벤트 처리
        $('#join_form').on('submit', function(e) {
            console.log("Form submit 이벤트 발생");
            e.preventDefault();
            joinSubmit(false);
        });

        // 확인 버튼 클릭 이벤트
        $(document).on('click', '._join_btn', function(e) {
            console.log("확인 버튼 클릭됨!");
            e.preventDefault();
            joinSubmit(false);
            return false;
        });

        console.log("이벤트 바인딩 완료");
        
        // 전역 함수로 등록 (필요한 경우)
        window.joinSubmit = joinSubmit;
    }
    
    // 페이지 로드 완료 후 실행
    console.log('🚀 스크립트 로드됨');
    
    if (document.readyState === 'loading') {
        console.log('📄 문서 로딩 중 - DOMContentLoaded 이벤트 대기');
        document.addEventListener('DOMContentLoaded', function() {
            console.log('📄 DOMContentLoaded 이벤트 발생');
            injectForm();
        });
    } else {
        console.log('📄 문서 이미 로드됨 - 즉시 실행');
        // 페이지가 이미 로드된 경우 약간의 지연 후 실행
        setTimeout(injectForm, 500);
    }
    
    // 추가적으로 페이지 변화를 감지하여 재시도
    let retryCount = 0;
    const maxRetries = 5;
    
    function retryInject() {
        if (retryCount >= maxRetries) {
            console.log('❌ 최대 재시도 횟수 초과');
            return;
        }
        
        retryCount++;
        console.log(`🔄 재시도 ${retryCount}/${maxRetries}`);
        
        setTimeout(function() {
            if (isPaymentPage() && !document.getElementById('user_info_edit_form')) {
                injectForm();
                if (!document.getElementById('user_info_edit_form')) {
                    retryInject();
                }
            }
        }, 2000 * retryCount);
    }
    
    // 첫 실행 후 성공하지 못하면 재시도 시작
    setTimeout(function() {
        if (isPaymentPage() && !document.getElementById('user_info_edit_form')) {
            console.log('🔄 초기 실행 실패 - 재시도 시작');
            retryInject();
        }
    }, 3000);
})();