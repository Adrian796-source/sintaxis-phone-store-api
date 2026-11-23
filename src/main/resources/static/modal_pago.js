// --- Modal de Pago Interactivo (Estilo Kyū) ---
function mostrarModalPago() {
    let modalExistente = document.getElementById('paymentModal');
    if (modalExistente) {
        modalExistente.remove();
    }

    const modal = document.createElement('div');
    modal.id = 'paymentModal';
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background: rgba(9, 10, 15, 0.85);
        z-index: 2500;
        display: flex;
        justify-content: center;
        align-items: center;
        backdrop-filter: blur(6px);
        animation: fadeIn 0.2s ease;
    `;

    modal.innerHTML = `
        <div style="background: var(--surface); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 2rem; width: 100%; max-width: 420px; box-shadow: var(--shadow-kyu); position: relative;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
                <h3 style="font-size: 1.25rem; font-weight: 600; color: var(--text-main); display: flex; align-items: center; gap: 0.5rem;">
                    💳 Selecciona Método de Pago
                </h3>
                <button class="btn btn-secondary" style="padding: 0.2rem 0.5rem; font-size: 0.75rem;" onclick="cerrarModalPago()">✕</button>
            </div>
            <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 1.5rem;">
                Elige cómo deseas abonar tu pedido en Sintaxis Store. Todas las transacciones son seguras.
            </p>
            <div style="display: flex; flex-direction: column; gap: 0.85rem;">
                <button class="btn btn-secondary pay-method-btn" onclick="seleccionarMetodoPago('EFECTIVO')" style="justify-content: flex-start; padding: 1rem; border-color: var(--border-color); background: var(--bg-canvas);">
                    <span style="font-size: 1.25rem; margin-right: 0.75rem;">💵</span>
                    <div style="text-align: left;">
                        <div style="font-weight: 600; color: var(--text-main);">Efectivo</div>
                        <div style="font-size: 0.75rem; color: var(--text-secondary);">Pago al recibir o retirar en tienda</div>
                    </div>
                </button>
                <button class="btn btn-secondary pay-method-btn" onclick="seleccionarMetodoPago('TARJETA')" style="justify-content: flex-start; padding: 1rem; border-color: var(--border-color); background: var(--bg-canvas);">
                    <span style="font-size: 1.25rem; margin-right: 0.75rem;">💳</span>
                    <div style="text-align: left;">
                        <div style="font-weight: 600; color: var(--text-main);">Tarjeta de Crédito / Débito</div>
                        <div style="font-size: 0.75rem; color: var(--text-secondary);">Visa, Mastercard, American Express</div>
                    </div>
                </button>
                <button class="btn btn-secondary pay-method-btn" onclick="seleccionarMetodoPago('TRANSFERENCIA')" style="justify-content: flex-start; padding: 1rem; border-color: var(--border-color); background: var(--bg-canvas);">
                    <span style="font-size: 1.25rem; margin-right: 0.75rem;">🏦</span>
                    <div style="text-align: left;">
                        <div style="font-weight: 600; color: var(--text-main);">Transferencia Bancaria</div>
                        <div style="font-size: 0.75rem; color: var(--text-secondary);">Alias CBU / CVU inmediato</div>
                    </div>
                </button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);

    // Cerrar al hacer clic fuera del contenido
    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            cerrarModalPago();
        }
    });
}

function cerrarModalPago() {
    const modal = document.getElementById('paymentModal');
    if (modal) {
        modal.remove();
    }
}

function seleccionarMetodoPago(metodo) {
    cerrarModalPago();
    procesarCompraConMetodo(metodo);
}

// --- Modal de Registro Post-Compra para Invitados (Estilo Kyū) ---
function mostrarModalRegistroInvitado(ventaCreada) {
    let modalExistente = document.getElementById('registerGuestModal');
    if (modalExistente) {
        modalExistente.remove();
    }

    const modal = document.createElement('div');
    modal.id = 'registerGuestModal';
    modal.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        background: rgba(9, 10, 15, 0.85);
        z-index: 2600;
        display: flex;
        justify-content: center;
        align-items: center;
        backdrop-filter: blur(6px);
        animation: fadeIn 0.2s ease;
    `;

    modal.innerHTML = `
        <div style="background: var(--surface); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 2rem; width: 100%; max-width: 450px; box-shadow: var(--shadow-kyu); position: relative; max-height: 90vh; overflow-y: auto;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <h3 style="font-size: 1.25rem; font-weight: 600; color: var(--text-main); display: flex; align-items: center; gap: 0.5rem;">
                    ✨ ¡Guarda tu compra y sé VIP!
                </h3>
                <button class="btn btn-secondary" style="padding: 0.2rem 0.5rem; font-size: 0.75rem;" onclick="cerrarModalRegistroInvitado()">✕</button>
            </div>
            <p style="color: var(--text-secondary); font-size: 0.875rem; margin-bottom: 1.25rem;">
                Tu compra <strong>#${ventaCreada.idVenta || 'N/A'}</strong> fue procesada con éxito. Completa tus datos para crear tu cuenta en Sintaxis Store, sumar puntos y acceder a beneficios exclusivos.
            </p>
            <form id="formRegistroInvitado" onsubmit="enviarRegistroInvitado(event, ${ventaCreada.idVenta})" style="display: flex; flex-direction: column; gap: 0.85rem;">
                <div>
                    <label style="display: block; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.3rem;">Nombre</label>
                    <input type="text" id="invitadoNombre" required placeholder="Tu nombre" style="width: 100%; padding: 0.75rem; background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); color: var(--text-main); font-size: 0.875rem;" />
                </div>
                <div>
                    <label style="display: block; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.3rem;">Apellido</label>
                    <input type="text" id="invitadoApellido" required placeholder="Tu apellido" style="width: 100%; padding: 0.75rem; background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); color: var(--text-main); font-size: 0.875rem;" />
                </div>
                <div>
                    <label style="display: block; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.3rem;">Email</label>
                    <input type="email" id="invitadoEmail" required placeholder="tu@email.com" style="width: 100%; padding: 0.75rem; background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); color: var(--text-main); font-size: 0.875rem;" />
                </div>
                <div>
                    <label style="display: block; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.3rem;">Contraseña</label>
                    <input type="password" id="invitadoPassword" required minlength="6" autocomplete="new-password" placeholder="Mínimo 6 caracteres" style="width: 100%; padding: 0.75rem; background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); color: var(--text-main); font-size: 0.875rem;" />
                </div>
                <div>
                    <label style="display: block; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.3rem;">Teléfono</label>
                    <input type="text" id="invitadoTelefono" required placeholder="+54 9 11..." style="width: 100%; padding: 0.75rem; background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); color: var(--text-main); font-size: 0.875rem;" />
                </div>
                <div>
                    <label style="display: block; font-size: 0.75rem; color: var(--text-secondary); margin-bottom: 0.3rem;">Dirección</label>
                    <input type="text" id="invitadoDireccion" required placeholder="Av. Corrientes 1234" style="width: 100%; padding: 0.75rem; background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); color: var(--text-main); font-size: 0.875rem;" />
                </div>
                <div style="display: flex; gap: 0.75rem; margin-top: 0.5rem;">
                    <button type="button" class="btn btn-secondary" style="flex: 1;" onclick="cerrarModalRegistroInvitado()">Omitir</button>
                    <button type="submit" class="btn btn-primary" style="flex: 1;">Crear Cuenta</button>
                </div>
            </form>
        </div>
    `;

    document.body.appendChild(modal);

    modal.addEventListener('click', (e) => {
        if (e.target === modal) {
            cerrarModalRegistroInvitado();
        }
    });
}

function cerrarModalRegistroInvitado() {
    const modal = document.getElementById('registerGuestModal');
    if (modal) {
        modal.remove();
    }
}

async function enviarRegistroInvitado(event, idVenta) {
    event.preventDefault();

    const nombre = document.getElementById('invitadoNombre').value.trim();
    const apellido = document.getElementById('invitadoApellido').value.trim();
    const email = document.getElementById('invitadoEmail').value.trim();
    const password = document.getElementById('invitadoPassword').value.trim();
    const telefono = document.getElementById('invitadoTelefono').value.trim();
    const direccion = document.getElementById('invitadoDireccion').value.trim();

    const payload = {
        nombre,
        apellido,
        email,
        password,
        telefono,
        direccion,
        rol: "CLIENTE"
    };

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || 'Error al registrar la cuenta.');
        }

        const data = await response.json();
        
        // Guardar token y sesión automáticamente
        localStorage.setItem('sintaxis_token', data.token);
        localStorage.setItem('sintaxis_user', data.email);
        localStorage.setItem('sintaxis_role', data.rol || 'ROLE_CLIENTE');
        if (data.idCliente) {
            localStorage.setItem('sintaxis_idCliente', data.idCliente);
        }

        cerrarModalRegistroInvitado();
        alert('🎉 ¡Cuenta creada con éxito! Ya estás registrado y tus puntos están acreditados.');
        
        // Refrescar UI si existe la función
        if (typeof verificarSesion === 'function') {
            verificarSesion();
        }
    } catch (err) {
        console.error(err);
        alert('Error al registrar: ' + err.message);
    }
}
