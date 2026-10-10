// Comportamentos de interface compartilhados por todas as páginas.
// Tudo é melhoria progressiva: sem JavaScript, links e formulários continuam funcionando
// (o formulário abre em página própria e a exclusão envia direto).
(function () {
    'use strict';

    // ---------------------------------------------------------------------
    // Barra de carregamento no topo
    // ---------------------------------------------------------------------
    var barra = document.createElement('div');
    barra.className = 'barra-carregamento';
    barra.setAttribute('aria-hidden', 'true');
    document.body.appendChild(barra);

    function iniciarCarregamento() {
        barra.classList.remove('concluida');
        void barra.offsetWidth; // aplica o estado inicial antes de animar
        barra.classList.add('ativa');
    }

    function concluirCarregamento() {
        barra.classList.remove('ativa');
        barra.classList.add('concluida');
        setTimeout(function () { barra.classList.remove('concluida'); }, 500);
    }

    function marcarOcupado(botao) {
        if (!botao) return;
        botao.setAttribute('aria-busy', 'true');
        setTimeout(function () { botao.disabled = true; }, 0); // depois do envio começar
    }

    function liberar(botao) {
        if (!botao) return;
        botao.removeAttribute('aria-busy');
        botao.disabled = false;
    }

    function abrirModal(dialogo) {
        document.documentElement.classList.add('modal-is-open');
        dialogo.showModal();
    }

    document.querySelectorAll('dialog').forEach(function (dialogo) {
        dialogo.addEventListener('close', function () {
            document.documentElement.classList.remove('modal-is-open');
        });
    });

    // ---------------------------------------------------------------------
    // Modal de formulário: <a href="/manutencoes/1/editar" data-modal>
    // Busca a página do formulário no servidor e exibe o cartão dentro do <dialog>.
    // O envio também é por fetch: com erro de validação o servidor devolve o formulário
    // com as mensagens (o modal só troca o conteúdo); com sucesso devolve um redirect.
    // ---------------------------------------------------------------------
    var dialogoForm = document.getElementById('dialogo-formulario');

    function exibirCartao(html) {
        var pagina = new DOMParser().parseFromString(html, 'text/html');
        var cartao = pagina.querySelector('article.cartao-form');
        if (!cartao) throw new Error('resposta sem formulário');
        dialogoForm.replaceChildren(document.importNode(cartao, true));
        var primeiroCampo = dialogoForm.querySelector('[aria-invalid="true"]')
            || dialogoForm.querySelector('select, input:not([type="hidden"])');
        if (primeiroCampo) primeiroCampo.focus();
    }

    document.addEventListener('click', function (e) {
        var link = e.target.closest('a[data-modal]');
        if (!link || !dialogoForm || e.ctrlKey || e.metaKey || e.shiftKey || e.button !== 0) return;
        e.preventDefault();
        marcarOcupado(link);
        iniciarCarregamento();
        fetch(link.href, { credentials: 'same-origin' })
            .then(function (resposta) {
                // redirected = sessão expirou e caiu no login: segue pela navegação normal
                if (!resposta.ok || resposta.redirected) throw resposta;
                return resposta.text();
            })
            .then(function (html) {
                abrirModal(dialogoForm);
                exibirCartao(html);
            })
            .catch(function () { location.href = link.href; })
            .finally(function () {
                liberar(link);
                concluirCarregamento();
            });
    });

    if (dialogoForm) {
        dialogoForm.addEventListener('click', function (e) {
            if (e.target.closest('[data-cancelar]')) {
                e.preventDefault();
                dialogoForm.close();
            }
        });
    }

    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (!dialogoForm || !dialogoForm.contains(form)) return;
        e.preventDefault();
        var botao = e.submitter || form.querySelector('[type="submit"]');
        marcarOcupado(botao);
        iniciarCarregamento();
        fetch(form.action, {
            method: 'POST',
            credentials: 'same-origin',
            redirect: 'manual',
            // urlencoded (e não multipart) para o filtro de CSRF ler o token do corpo
            body: new URLSearchParams(new FormData(form))
        })
            .then(function (resposta) {
                if (resposta.type === 'opaqueredirect') {
                    // salvou: o redirect leva à listagem, que exibe a mensagem de sucesso
                    location.href = form.getAttribute('data-sucesso');
                    return;
                }
                if (!resposta.ok) throw resposta;
                return resposta.text().then(function (html) {
                    exibirCartao(html);
                    concluirCarregamento();
                });
            })
            .catch(function () {
                // qualquer falha inesperada: envia pelo caminho normal, sem modal
                HTMLFormElement.prototype.submit.call(form);
            });
    });

    // ---------------------------------------------------------------------
    // Modal de confirmação: <form data-confirmar="Título" data-detalhe="...">
    // Intercepta o envio, abre o <dialog> e só envia se o usuário confirmar.
    // ---------------------------------------------------------------------
    var dialogo = document.getElementById('dialogo-confirmacao');
    var formularioPendente = null;

    document.addEventListener('submit', function (e) {
        var form = e.target;
        if (e.defaultPrevented || !dialogo || !form.hasAttribute('data-confirmar') || form.dataset.confirmado === 'sim') {
            return;
        }
        e.preventDefault();
        formularioPendente = form;
        dialogo.querySelector('[data-titulo]').textContent = form.getAttribute('data-confirmar');
        dialogo.querySelector('[data-detalhe]').textContent = form.getAttribute('data-detalhe') || '';
        abrirModal(dialogo);
        dialogo.querySelector('[data-fechar]').focus();
    });

    if (dialogo) {
        dialogo.querySelector('[data-confirmar-acao]').addEventListener('click', function () {
            if (!formularioPendente) return;
            marcarOcupado(this);
            dialogo.querySelector('[data-fechar]').disabled = true;
            formularioPendente.dataset.confirmado = 'sim';
            formularioPendente.requestSubmit();
        });

        dialogo.querySelector('[data-fechar]').addEventListener('click', function () {
            dialogo.close();
        });

        // clique no fundo escuro (fora do cartão) fecha a confirmação
        dialogo.addEventListener('click', function (e) {
            if (e.target === dialogo) dialogo.close();
        });

        dialogo.addEventListener('close', function () {
            formularioPendente = null;
        });
    }

    // ---------------------------------------------------------------------
    // Envio de formulários e navegação comuns: spinner no botão + barra no topo
    // ---------------------------------------------------------------------
    document.addEventListener('submit', function (e) {
        if (e.defaultPrevented) return;
        marcarOcupado(e.submitter || e.target.querySelector('[type="submit"]'));
        iniciarCarregamento();
    });

    document.addEventListener('click', function (e) {
        var link = e.target.closest('a[href]');
        if (!link || e.defaultPrevented || e.button !== 0 || e.ctrlKey || e.metaKey || e.shiftKey || e.altKey) return;
        if (link.target && link.target !== '_self') return;
        var destino = new URL(link.href, location.href);
        if (destino.origin !== location.origin) return;
        if (destino.pathname === location.pathname && destino.search === location.search && destino.hash) return;
        iniciarCarregamento();
    });

    // Voltar pelo navegador pode reaproveitar a página do cache: limpa os estados de carregamento
    window.addEventListener('pageshow', function () {
        barra.classList.remove('ativa', 'concluida');
        document.querySelectorAll('[aria-busy="true"]').forEach(liberar);
        document.querySelectorAll('form[data-confirmado]').forEach(function (form) {
            delete form.dataset.confirmado;
        });
        document.querySelectorAll('dialog[open]').forEach(function (d) { d.close(); });
    });

    // ---------------------------------------------------------------------
    // Menu do usuário: fecha ao clicar fora
    // ---------------------------------------------------------------------
    document.addEventListener('click', function (e) {
        document.querySelectorAll('details.menu-usuario[open]').forEach(function (menu) {
            if (!menu.contains(e.target)) menu.removeAttribute('open');
        });
    });
})();
