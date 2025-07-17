// dragNdrop.js - 단어/문장 드래그 앤 드롭 순서 변경 및 서버 저장

function makeListDraggable(listSelector, itemSelector, saveUrl) {
    const list = document.querySelector(listSelector);
    if (!list) return;

    // === 드래그앤드롭 비활성화 조건 ===
    // day/level select가 있으면 전체일 때 비활성화
    const daySelect = document.getElementById(listSelector === '#word-list' ? 'word-day-select' : 'sentence-day-select');
    const levelSelect = document.getElementById(listSelector === '#word-list' ? 'word-level-select' : 'sentence-level-select');
    if ((daySelect && daySelect.value === 'all') || (levelSelect && levelSelect.value === 'all')) {
        // 모든 아이템에서 draggable 속성 제거
        list.querySelectorAll(itemSelector).forEach(item => {
            item.removeAttribute('draggable');
            item.classList.remove('dragElem', 'over', 'drag-insert-line');
        });
        return;
    }

    let dragSrcEl = null;
    let insertLine = null;

    function handleDragStart(e) {
        dragSrcEl = this;
        e.dataTransfer.effectAllowed = 'move';
        e.dataTransfer.setData('text/html', this.outerHTML);
        this.classList.add('dragElem');
    }

    function handleDragOver(e) {
        if (e.preventDefault) e.preventDefault();
        this.classList.add('over');
        e.dataTransfer.dropEffect = 'move';
        // === 드롭 위치 가이드 라인 ===
        showInsertLine(this, e);
        return false;
    }

    function handleDragEnter(e) {
        this.classList.add('over');
    }

    function handleDragLeave(e) {
        this.classList.remove('over');
        removeInsertLine();
    }

    function handleDrop(e) {
        if (e.stopPropagation) e.stopPropagation();
        if (dragSrcEl !== this) {
            this.parentNode.removeChild(dragSrcEl);
            const dropHTML = e.dataTransfer.getData('text/html');
            // === drop 위치에 따라 before/after 결정 ===
            if (insertLine && insertLine.classList.contains('insert-after')) {
                this.insertAdjacentHTML('afterend', dropHTML);
                const droppedElem = this.nextSibling;
                addDnDEvents(droppedElem);
            } else {
                this.insertAdjacentHTML('beforebegin', dropHTML);
                const droppedElem = this.previousSibling;
                addDnDEvents(droppedElem);
            }
        }
        this.classList.remove('over');
        removeInsertLine();
        updateOrderOnServer();
        return false;
    }

    function handleDragEnd(e) {
        this.classList.remove('over');
        this.classList.remove('dragElem');
        const items = list.querySelectorAll(itemSelector);
        items.forEach(item => item.classList.remove('over'));
        removeInsertLine();
    }

    function addDnDEvents(elem) {
        elem.setAttribute('draggable', true);
        elem.addEventListener('dragstart', handleDragStart, false);
        elem.addEventListener('dragenter', handleDragEnter, false);
        elem.addEventListener('dragover', handleDragOver, false);
        elem.addEventListener('dragleave', handleDragLeave, false);
        elem.addEventListener('drop', handleDrop, false);
        elem.addEventListener('dragend', handleDragEnd, false);
    }

    function updateOrderOnServer() {
        const items = list.querySelectorAll(itemSelector);
        const order = Array.from(items).map((item, idx) => ({
            id: item.dataset.id,
            order: idx + 1
        }));
        fetch(saveUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(order)
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                console.log('순서 저장 성공');
            } else {
                alert('순서 저장 실패: ' + (data.message || '오류'));
            }
        })
        .catch(err => {
            alert('순서 저장 중 오류 발생');
            console.error(err);
        });
    }

    // === 드롭 위치 가이드 라인 ===
    function showInsertLine(target, e) {
        removeInsertLine();
        insertLine = document.createElement('div');
        insertLine.className = 'drag-insert-line';
        // 마우스 위치에 따라 before/after 결정
        const rect = target.getBoundingClientRect();
        const offset = e.clientY - rect.top;
        if (offset > rect.height / 2) {
            insertLine.classList.add('insert-after');
            target.parentNode.insertBefore(insertLine, target.nextSibling);
        } else {
            insertLine.classList.add('insert-before');
            target.parentNode.insertBefore(insertLine, target);
        }
    }
    function removeInsertLine() {
        if (insertLine && insertLine.parentNode) {
            insertLine.parentNode.removeChild(insertLine);
        }
        insertLine = null;
    }

    // 초기화
    const items = list.querySelectorAll(itemSelector);
    items.forEach(addDnDEvents);
}

// 단어 리스트 예시: makeListDraggable('#word-list', '.word-item', '/api/dragndrop/words-order');
// 문장 리스트 예시: makeListDraggable('#sentence-list', '.sentence-item', '/api/dragndrop/sentences-order');

document.addEventListener('DOMContentLoaded', function() {
    makeListDraggable('#word-list', '.word-item', '/api/dragndrop/words-order');
    makeListDraggable('#sentence-list', '.sentence-item', '/api/dragndrop/sentences-order');
});
