// ==================== GESTIÓN DE INACTIVIDAD (IDLE TIMEOUT - 5 MINUTOS) ====================
let inactivityTimer = null;
let countdownInterval = null;
let remainingSeconds = 30;
const IDLE_TIME_MS = 4 * 60 * 1000 + 30 * 1000; // 4 minutos 30 segundos antes de mostrar advertencia
const COUNTDOWN_TIME_S = 30; // 30 segundos de cuenta regresiva en el modal

function iniciarTemporizadorInactividad() {
    // Solo activar si hay una sesión iniciada
    if (!localStorage.getItem('sintaxis_token')) {
        detenerTemporizadorInactividad();
        return;
    }

    reiniciarTemporizadorInactividad();

    // Eventos que indican actividad del usuario
    const eventos = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart', 'input'];
    eventos.forEach(evento => {
        window.removeEventListener(evento, registrarActividadUsuario);
        window.addEventListener(evento, registrarActividadUsuario, { passive: true });
    });
}

function registrarActividadUsuario() {
    // Si el modal de inactividad está abierto, ignoramos eventos aleatorios de fondo
    const modal = document.getElementById('inactivityModal');
    if (modal && modal.style.display === 'flex') {
        return;
    }
    reiniciarTemporizadorInactividad();
}

function reiniciarTemporizadorInactividad() {
    if (!localStorage.getItem('sintaxis_token')) return;

    clearTimeout(inactivityTimer);
    clearInterval(countdownInterval);

    // Ocultar modal si estuviera visible
    const modal = document.getElementById('inactivityModal');
    if (modal) modal.style.display = 'none';

    inactivityTimer = setTimeout(() => {
        mostrarModalInactividad();
    }, IDLE_TIME_MS);
}

function mostrarModalInactividad() {
    if (!localStorage.getItem('sintaxis_token')) return;

    const modal = document.getElementById('inactivityModal');
    const countdownEl = document.getElementById('inactivityCountdown');
    if (!modal) return;

    remainingSeconds = COUNTDOWN_TIME_S;
    if (countdownEl) countdownEl.textContent = remainingSeconds;
    modal.style.display = 'flex';

    clearInterval(countdownInterval);
    countdownInterval = setInterval(() => {
        remainingSeconds--;
        if (countdownEl) countdownEl.textContent = remainingSeconds;

        if (remainingSeconds <= 0) {
            clearInterval(countdownInterval);
            cerrarSesionPorInactividad();
        }
    }, 1000);
}

function resetInactivityTimer() {
    const modal = document.getElementById('inactivityModal');
    if (modal) modal.style.display = 'none';
    clearInterval(countdownInterval);
    reiniciarTemporizadorInactividad();
    mostrarToast('Sesión extendida por actividad.', 'info');
}

function cerrarSesionPorInactividad() {
    clearTimeout(inactivityTimer);
    clearInterval(countdownInterval);

    const modal = document.getElementById('inactivityModal');
    if (modal) modal.style.display = 'none';

    localStorage.removeItem('sintaxis_token');
    localStorage.removeItem('sintaxis_rol');
    localStorage.removeItem('sintaxis_email');
    localStorage.removeItem('sintaxis_idCliente');
    
    actualizarUIAuth();
    
    // Cerrar modales abiertos por si acaso
    const adminModal = document.getElementById('adminModal');
    if (adminModal) adminModal.style.display = 'none';
    const perfilModal = document.getElementById('miPerfilModal');
    if (perfilModal) perfilModal.style.display = 'none';

    mostrarToast('⚠️ Sesión cerrada automáticamente por 5 minutos de inactividad.', 'error');
}

function detenerTemporizadorInactividad() {
    clearTimeout(inactivityTimer);
    clearInterval(countdownInterval);
    const modal = document.getElementById('inactivityModal');
    if (modal) modal.style.display = 'none';
}
