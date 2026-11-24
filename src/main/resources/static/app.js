window.celularesGlobal = window.celularesGlobal || [];
window.accesoriosGlobal = window.accesoriosGlobal || [];
window.categoriaActiva = window.categoriaActiva || 'todos';
window.subcategoriaActiva = window.subcategoriaActiva || 'Todas';

async function cargarProductos() {
    const gridCelulares = document.getElementById('gridCelulares');
    const containerAccesoriosSubcats = document.getElementById('containerAccesoriosSubcats');
    
    const loadingHtml = `<div class="empty-state" style="grid-column: 1 / -1; padding: 2rem; text-align: center; color: var(--text-secondary);">Conectando con el núcleo API (/api/celulares y /api/accesorios)...</div>`;
    if (gridCelulares) gridCelulares.innerHTML = loadingHtml;
    if (containerAccesoriosSubcats) containerAccesoriosSubcats.innerHTML = loadingHtml;

    try {
        const [resCelulares, resAccesorios] = await Promise.all([
            fetch('/api/celulares'),
            fetch('/api/accesorios')
        ]);

        let dataCelulares = [];
        let dataAccesorios = [];

        if (resCelulares.ok) {
            const jsonC = await resCelulares.json();
            dataCelulares = Array.isArray(jsonC) ? jsonC : (jsonC.content || []);
        }

        if (resAccesorios.ok) {
            const jsonA = await resAccesorios.json();
            dataAccesorios = Array.isArray(jsonA) ? jsonA : (jsonA.content || []);
        }

        celularesGlobal = dataCelulares.map(p => ({ ...p, _tipo: 'celular' }));
        accesoriosGlobal = dataAccesorios.map(p => ({ ...p, _tipo: 'accesorio' }));

        filtrarProductos();
        actualizarMetricas([...celularesGlobal, ...accesoriosGlobal]);
        actualizarContadorStockCritico();
    } catch (error) {
        console.error(error);
        const errorHtml = `<div class="empty-state" style="grid-column: 1 / -1; padding: 2rem; text-align: center; color: var(--danger-rose);">
            <strong>Error de conexión con el servidor</strong>
            <p>No se pudo obtener la información de <code>/api/celulares</code> y <code>/api/accesorios</code>. Verifique el estado de Spring Boot.</p>
        </div>`;
        if (gridCelulares) gridCelulares.innerHTML = errorHtml;
        if (containerAccesoriosSubcats) containerAccesoriosSubcats.innerHTML = errorHtml;
    }
}

function cambiarTab(tab) {
    categoriaActiva = tab;
    subcategoriaActiva = 'Todas';
    
    const subContainer = document.getElementById('subcategoriaContainer');
    if (tab === 'accesorios') {
        subContainer.style.display = 'flex';
        cambiarSubcategoria('Todas', false); // No dispara doble filtro innecesario
    } else {
        subContainer.style.display = 'none';
    }

    ['todos', 'celulares', 'accesorios'].forEach(t => {
        const btn = document.getElementById(`tab-${t}`);
        if (t === tab) {
            btn.style.background = 'var(--accent-cyan)';
            btn.style.color = '#090A0F';
            btn.style.fontWeight = '600';
        } else {
            btn.style.background = 'transparent';
            btn.style.color = 'var(--text-secondary)';
            btn.style.fontWeight = '400';
        }
    });
    filtrarProductos();
}

function cambiarSubcategoria(sub, ejecutarFiltro = true) {
    subcategoriaActiva = sub;
    const botones = document.querySelectorAll('.subcat-btn');
    botones.forEach(btn => {
        if (btn.textContent.trim() === sub) {
            btn.style.background = 'var(--accent-cyan)';
            btn.style.color = '#090A0F';
            btn.style.fontWeight = '600';
            btn.style.border = 'none';
        } else {
            btn.style.background = 'transparent';
            btn.style.color = 'var(--text-secondary)';
            btn.style.fontWeight = '400';
            btn.style.border = '1px solid var(--border-color)';
        }
    });
    if (ejecutarFiltro) {
        filtrarProductos();
    }
}

function filtrarProductos() {
    let fuente = [];
    if (categoriaActiva === 'todos') {
        fuente = [...celularesGlobal, ...accesoriosGlobal];
    } else if (categoriaActiva === 'celulares') {
        fuente = celularesGlobal;
    } else if (categoriaActiva === 'accesorios') {
        fuente = accesoriosGlobal;
        if (subcategoriaActiva !== 'Todas') {
            fuente = fuente.filter(p => {
                const sub = p.subcategoriaNombre || p.subcategoria || '';
                const subStr = typeof sub === 'string' ? sub : (sub.nombre || '');
                const subNorm = subStr.trim().toLowerCase();
                const buscada = subcategoriaActiva.trim().toLowerCase();
                return subNorm === buscada || (subNorm && subNorm.includes(buscada));
            });
        }
    }

    const query = document.getElementById('searchInput').value.trim().toLowerCase();
    const estadoFiltro = document.getElementById('filterEstado').value;
    const sortFiltro = document.getElementById('filterSort') ? document.getElementById('filterSort').value : 'default';

    const filtrados = fuente.filter(p => {
        const matchQuery = !query || 
            (p.nombre && p.nombre.toLowerCase().includes(query)) ||
            (p.marca && p.marca.toLowerCase().includes(query)) ||
            (p.descripcion && p.descripcion.toLowerCase().includes(query)) ||
            (p.modelo && p.modelo.toLowerCase().includes(query));

        let matchEstado = true;
        if (estadoFiltro === 'activos') {
            matchEstado = p.activo === true;
        } else if (estadoFiltro === 'stock-bajo') {
            matchEstado = p.stock <= p.stockMinimo;
        }

        return matchQuery && matchEstado;
    });

    // Aplicar Ordenamiento (Sorting)
    if (sortFiltro === 'precio-asc') {
        filtrados.sort((a, b) => (a.precio || 0) - (b.precio || 0));
    } else if (sortFiltro === 'precio-desc') {
        filtrados.sort((a, b) => (b.precio || 0) - (a.precio || 0));
    } else if (sortFiltro === 'nombre-az') {
        filtrados.sort((a, b) => (a.nombre || '').localeCompare(b.nombre || ''));
    } else if (sortFiltro === 'stock-desc') {
        filtrados.sort((a, b) => (b.stock || 0) - (a.stock || 0));
    }

    renderizarProductos(filtrados);
}

function renderizarProductos(productos) {
    const sectionCelulares = document.getElementById('sectionCelulares');
    const sectionAccesorios = document.getElementById('sectionAccesorios');
    const gridCelulares = document.getElementById('gridCelulares');
    const containerAccesoriosSubcats = document.getElementById('containerAccesoriosSubcats');
    
    const countCelularesEl = document.getElementById('countCelulares');
    const countAccesoriosEl = document.getElementById('countAccesorios');

    // Separar celulares y accesorios
    const celularesList = productos.filter(p => p._tipo === 'celular');
    const accesoriosList = productos.filter(p => p._tipo === 'accesorio');

    countCelularesEl.textContent = celularesList.length;
    countAccesoriosEl.textContent = accesoriosList.length;

    // Controlar visibilidad según pestaña activa (todos, celulares, accesorios)
    if (categoriaActiva === 'celulares') {
        sectionCelulares.style.display = 'block';
        sectionAccesorios.style.display = 'none';
    } else if (categoriaActiva === 'accesorios') {
        sectionCelulares.style.display = 'none';
        sectionAccesorios.style.display = 'block';
    } else {
        sectionCelulares.style.display = 'block';
        sectionAccesorios.style.display = 'block';
    }

    // Renderizar Celulares Grid
    if (celularesList.length === 0) {
        gridCelulares.innerHTML = `<div class="empty-state" style="grid-column: 1 / -1; padding: 2rem; text-align: center; color: var(--text-secondary);">No hay celulares disponibles que coincidan con la búsqueda.</div>`;
    } else {
        gridCelulares.innerHTML = celularesList.map(p => crearTarjetaProducto(p)).join('');
    }

    // Renderizar Accesorios agrupados por subcategoría
    if (accesoriosList.length === 0) {
        containerAccesoriosSubcats.innerHTML = `<div class="empty-state" style="padding: 2rem; text-align: center; color: var(--text-secondary);">No hay accesorios disponibles que coincidan con la búsqueda.</div>`;
    } else {
        // Agrupar accesorios por subcategoría
        const grupos = {};
        accesoriosList.forEach(acc => {
            let sub = acc.subcategoriaNombre || acc.subcategoria || 'General';
            if (typeof sub === 'object' && sub !== null) {
                sub = sub.nombre || 'General';
            }
            const subStr = String(sub).trim() || 'General';
            if (!grupos[subStr]) grupos[subStr] = [];
            grupos[subStr].push(acc);
        });

        let htmlGrupos = '';
        for (const [subcatNombre, items] of Object.entries(grupos)) {
            htmlGrupos += `
                <div class="subcat-group-wrapper" style="margin-bottom: 1.5rem;">
                    <div class="subcat-group-title">
                        📁 ${escapeHtml(subcatNombre)} <span>${items.length}</span>
                    </div>
                    <div class="product-grid" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.5rem;">
                        ${items.map(p => crearTarjetaProducto(p)).join('')}
                    </div>
                </div>
            `;
        }
        containerAccesoriosSubcats.innerHTML = htmlGrupos;
    }
}

function crearTarjetaProducto(p) {
    const stockActual = p.stock !== undefined ? p.stock : 0;
    const esAgotado = stockActual <= 0;
    const esStockBajo = !esAgotado && (stockActual <= p.stockMinimo);

    const imagenDefault = p.imagenUrl && p.imagenUrl.trim() !== '' 
        ? (p.imagenUrl.startsWith('http://localhost:8080/api/productos/') 
            ? p.imagenUrl.replace('/api/productos/', '/api/imagenes/') 
            : p.imagenUrl)
        : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&auto=format&fit=crop&q=80';

    let badgeHtml = '';
    if (p._tipo === 'celular') {
        badgeHtml = `<span class="badge badge-active" style="background: rgba(56, 189, 248, 0.15); color: var(--accent-cyan); border-color: rgba(56,189,248,0.3);">Celular</span>`;
    } else {
        const sub = p.subcategoriaNombre || p.subcategoria || 'Accesorio';
        badgeHtml = `<span class="badge" style="background: rgba(236, 72, 153, 0.15); color: #ec4899; border-color: rgba(236,72,153,0.3);">${escapeHtml(sub)}</span>`;
    }

    if (esAgotado) {
        badgeHtml += ` <span class="badge" style="background: rgba(244, 63, 94, 0.2); color: var(--danger-rose); border-color: rgba(244, 63, 94, 0.4); font-weight: 700; box-shadow: 0 0 10px rgba(244,63,94,0.3);">AGOTADO</span>`;
    } else if (esStockBajo) {
        badgeHtml += ` <span class="badge badge-warning" style="margin-left: 0.25rem;">Stock Bajo</span>`;
    }

    const botonAgregarHtml = esAgotado 
        ? `<button class="btn btn-secondary" style="padding: 0.5rem 1rem; font-size: 0.75rem; opacity: 0.5; cursor: not-allowed;" disabled>🚫 Agotado</button>`
        : `<button class="btn btn-primary" style="padding: 0.5rem 1rem; font-size: 0.75rem;" onclick='agregarAlCarrito(${JSON.stringify(p).replace(/'/g, "&#39;")})'>🛒 Agregar</button>`;

    return `
        <div class="product-card" ${esAgotado ? 'style="opacity: 0.85; cursor: pointer;"' : 'style="cursor: pointer;"'} onclick='abrirModalDetalleProducto(${JSON.stringify(p).replace(/'/g, "&#39;")})'>
            <div class="product-card-img-container">
                <div class="product-card-badge">${badgeHtml}</div>
                <img src="${imagenDefault}" alt="${escapeHtml(p.nombre)}" class="product-card-img" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&auto=format&fit=crop&q=80';">
            </div>
            <div class="product-card-body">
                <div class="product-card-brand">${escapeHtml(p.marca || 'Genérica')}</div>
                <div class="product-card-title">${escapeHtml(p.nombre)}</div>
                <div class="product-card-desc" title="${escapeHtml(p.descripcion || '')}">${escapeHtml(p.descripcion || 'Sin descripción disponible.')}</div>
                <div class="product-card-footer">
                    <div class="product-card-price">$${Number(p.precio || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 })}</div>
                    <div onclick="event.stopPropagation()">
                        ${botonAgregarHtml}
                    </div>
                </div>
            </div>
        </div>
    `;
}

function actualizarMetricas(productos) {
    const total = productos.length;
    const activos = productos.filter(p => p.activo).length;

    document.getElementById('metric-total').textContent = total;
    document.getElementById('metric-activos').textContent = activos;
}

function actualizarContadorStockCritico() {
    const todos = [...celularesGlobal, ...accesoriosGlobal];
    const criticos = todos.filter(p => {
        const stock = p.stock !== undefined ? p.stock : 0;
        const minimo = p.stockMinimo !== undefined ? p.stockMinimo : 5;
        return stock <= minimo;
    });
    const badgeEl = document.getElementById('adminStockCriticoCount');
    if (badgeEl) {
        badgeEl.textContent = criticos.length;
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>'"]/g, 
        tag => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#39;', '"': '&quot;' }[tag] || tag)
    );
}

// --- Carrito de Compras ---
let carrito = JSON.parse(localStorage.getItem('sintaxis_carrito')) || [];

function toggleCartDrawer() {
    const drawer = document.getElementById('cartDrawer');
    drawer.classList.toggle('open');
}

function guardarCarrito() {
    localStorage.setItem('sintaxis_carrito', JSON.stringify(carrito));
    actualizarContadorCarrito();
    renderizarCarrito();
}

function actualizarContadorCarrito() {
    const totalItems = carrito.reduce((sum, item) => sum + item.cantidad, 0);
    document.getElementById('cart-badge').textContent = totalItems;
}

function agregarAlCarrito(producto) {
    const idProd = producto.idProducto || producto.id;
    const existente = carrito.find(item => (item.idProducto || item.id) === idProd);
    
    const stockDisponible = producto.stock !== undefined ? producto.stock : 999;

    if (existente) {
        if (existente.cantidad < stockDisponible) {
            existente.cantidad++;
            mostrarToast(`Se agregó otra unidad de "${producto.nombre}" al carrito.`, 'success');
        } else {
            mostrarToast(`Stock máximo alcanzado (${stockDisponible} unidades disponibles).`, 'error');
            return;
        }
    } else {
        if (stockDisponible > 0) {
            carrito.push({ ...producto, cantidad: 1 });
            mostrarToast(`"${producto.nombre}" añadido al carrito.`, 'success');
        } else {
            mostrarToast(`Lo sentimos, "${producto.nombre}" está agotado.`, 'error');
            return;
        }
    }
    guardarCarrito();
    
    const drawer = document.getElementById('cartDrawer');
    if (!drawer.classList.contains('open')) {
        toggleCartDrawer();
    }
}

function cambiarCantidad(idProducto, delta) {
    const index = carrito.findIndex(item => (item.idProducto || item.id) === idProducto);
    if (index !== -1) {
        const item = carrito[index];
        const stockDispo = item.stock !== undefined ? item.stock : 999;

        const nuevaCantidad = item.cantidad + delta;
        if (nuevaCantidad <= 0) {
            carrito.splice(index, 1);
            mostrarToast(`Producto eliminado del carrito.`, 'info');
        } else if (nuevaCantidad > stockDispo) {
            mostrarToast(`Stock insuficiente. Solo hay ${stockDispo} unidades disponibles.`, 'error');
            item.cantidad = stockDispo;
        } else {
            item.cantidad = nuevaCantidad;
        }
        guardarCarrito();
    }
}

function eliminarDelCarrito(idProducto) {
    carrito = carrito.filter(item => (item.idProducto || item.id) !== idProducto);
    guardarCarrito();
}

function renderizarCarrito() {
    const container = document.getElementById('cartItemsContainer');
    const totalAmountEl = document.getElementById('cartTotalAmount');

    if (carrito.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="padding: 2rem 1rem;">
                <p>Tu carrito está vacío</p>
            </div>
        `;
        totalAmountEl.textContent = '$0.00';
        return;
    }

    let total = 0;
    container.innerHTML = carrito.map(item => {
        const subtotal = item.precio * item.cantidad;
        total += subtotal;
        const img = item.imagenUrl || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=100&auto=format&fit=crop&q=60';
        const idProd = item.idProducto || item.id;

        return `
            <div class="cart-item">
                <img src="${img}" alt="${item.nombre}" onerror="this.style.display='none'">

                <div class="cart-item-details">
                    <div class="cart-item-title">${escapeHtml(item.nombre)}</div>
                    <div class="cart-item-price">$${Number(item.precio).toLocaleString('es-ES', { minimumFractionDigits: 2 })} c/u</div>
                    <div class="cart-item-controls">
                        <button class="qty-btn" onclick="cambiarCantidad(${idProd}, -1)">-</button>
                        <span class="mono" style="font-size: 0.8rem; font-weight: 600;">${item.cantidad}</span>
                        <button class="qty-btn" onclick="cambiarCantidad(${idProd}, 1)">+</button>
                        <button class="btn btn-secondary" style="padding: 0.1rem 0.4rem; font-size: 0.7rem; margin-left: auto; color: var(--danger-rose); border-color: rgba(244,63,94,0.3);" onclick="eliminarDelCarrito(${idProd})">Eliminar</button>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    totalAmountEl.textContent = '$' + total.toLocaleString('es-ES', { minimumFractionDigits: 2 });
}

function finalizarCompra() {
    if (carrito.length === 0) {
        alert('El carrito está vacío.');
        return;
    }
    mostrarModalPago();
}

async function procesarCompraConMetodo(metodoPago) {
    const token = localStorage.getItem('sintaxis_token');
    const idCliente = localStorage.getItem('sintaxis_idCliente');

    let finalIdCliente = null;
    if (idCliente && idCliente !== 'null' && idCliente !== 'undefined') {
        finalIdCliente = Number(idCliente);
    }

    const metodoFinal = ['EFECTIVO', 'TARJETA', 'TRANSFERENCIA'].includes(metodoPago) ? metodoPago : 'EFECTIVO';

    const detallesRequest = carrito.map(item => ({
        idProducto: item.idProducto || item.id,
        cantidad: item.cantidad
    }));

    const ventaPayload = {
        idCliente: finalIdCliente,
        metodoPago: metodoFinal,
        estado: 'Pendiente',
        detalles: detallesRequest
    };

    try {
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const response = await fetch('/api/ventas', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(ventaPayload)
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || 'Error al procesar la venta en el servidor.');
        }

        const ventaCreada = await response.json();
        
        // Vaciado automático inmediato y limpio del carrito tras el éxito
        carrito = [];
        guardarCarrito();
        renderizarCarrito();
        toggleCartDrawer();

        // Mensaje de éxito profesional con invitación a registrarse si es invitado
        let mensajeExito = `🎉 ¡Compra procesada con éxito!\n\nID de Venta: #${ventaCreada.idVenta || 'N/A'}\nTotal Pagado: $${ventaCreada.total ? ventaCreada.total.toLocaleString('es-ES', { minimumFractionDigits: 2 }) : '0.00'}\n\n`;
        if (!finalIdCliente || !token) {
            mensajeExito += `💡 ¡Regístrate o inicia sesión en Sintaxis Store para acumular puntos, obtener beneficios VIP y consultar todo tu historial de compras!`;
        } else {
            mensajeExito += `¡Gracias por tu compra frecuente en Sintaxis Store!`;
        }

        alert(mensajeExito);

        // Si es invitado, mostrar modal profesional de registro post-compra
        if (!finalIdCliente || !token) {
            if (typeof mostrarModalRegistroInvitado === 'function') {
                mostrarModalRegistroInvitado(ventaCreada);
            }
        }
    } catch (err) {
        console.error(err);
        alert('Error al finalizar compra: ' + err.message);
    }
}

// --- Autenticación y Control de Sesión ---
function actualizarUIAuth() {
    const container = document.getElementById('auth-action-container');
    const syncContainer = document.getElementById('sync-action-container');
    const token = localStorage.getItem('sintaxis_token');
    const rol = localStorage.getItem('sintaxis_rol');

    if (syncContainer) {
        if (token && (rol === 'ROLE_ADMIN' || rol === 'ROLE_EMPLEADO')) {
            syncContainer.style.display = 'flex';
            syncContainer.innerHTML = `
                <button class="btn btn-secondary" onclick="cargarProductos()">
                    ⚡ Sincronizar API
                </button>
            `;
        } else {
            syncContainer.style.display = 'none';
            syncContainer.innerHTML = '';
        }
    }

    if (!token) {
        container.innerHTML = `
            <button class="btn btn-secondary" onclick="toggleLoginModal(true)">
                🔑 Iniciar Sesión
            </button>
        `;
    } else {
        let html = `
            <button class="btn btn-secondary" style="color: var(--danger-rose); border-color: rgba(244,63,94,0.3);" onclick="cerrarSesion()">
                🚪 Cerrar Sesión
            </button>
            <button class="btn btn-primary" style="background: rgba(56, 189, 248, 0.15); color: var(--accent-cyan); border-color: rgba(56,189,248,0.3);" onclick="abrirModalMiPerfil()">
                👤 Mi Perfil
            </button>
        `;
        if (rol === 'ROLE_ADMIN' || rol === 'ROLE_EMPLEADO') {
            html += `
                <button class="btn btn-primary" style="background: rgba(16, 185, 129, 0.15); color: var(--success-emerald); border-color: rgba(16, 185, 129, 0.3);" onclick="irPanelAdmin()">
                    ⚙️ Panel Admin
                </button>
            `;
        }
        container.innerHTML = html;
    }
}

function toggleLoginModal(abrir) {
    const modal = document.getElementById('loginModal');
    const errorMsg = document.getElementById('loginErrorMsg');
    errorMsg.style.display = 'none';
    errorMsg.textContent = '';
    if (abrir) {
        modal.style.display = 'flex';
        document.getElementById('loginEmail').focus();
    } else {
        modal.style.display = 'none';
        document.getElementById('loginForm').reset();
    }
}

async function ejecutarLogin(event) {
    event.preventDefault();
    const email = document.getElementById('loginEmail').value.trim();
    const password = document.getElementById('loginPassword').value;
    const errorMsg = document.getElementById('loginErrorMsg');

    errorMsg.style.display = 'none';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || 'Credenciales inválidas o error en el servidor.');
        }

        const data = await response.json();
        
        let rolGuardar = data.rol;
        if (rolGuardar && !rolGuardar.startsWith('ROLE_')) {
            rolGuardar = 'ROLE_' + rolGuardar;
        }

        localStorage.setItem('sintaxis_token', data.token);
        localStorage.setItem('sintaxis_rol', rolGuardar);
        localStorage.setItem('sintaxis_email', data.email);
        if (data.idCliente) {
            localStorage.setItem('sintaxis_idCliente', data.idCliente);
        } else {
            localStorage.removeItem('sintaxis_idCliente');
        }

        toggleLoginModal(false);
        actualizarUIAuth();
        iniciarTemporizadorInactividad();
        mostrarToast(`¡Bienvenido de nuevo, ${data.email}!`, 'success');
    } catch (err) {
        console.error(err);
        errorMsg.textContent = err.message || 'Error al iniciar sesión.';
        errorMsg.style.display = 'block';
    }
}

async function cerrarSesion() {
    const token = localStorage.getItem('sintaxis_token');
    try {
        if (token) {
            await fetch('/api/auth/logout', {
                method: 'POST',
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            });
        }
    } catch (e) {
        console.error('Error al notificar logout al servidor', e);
    } finally {
        localStorage.removeItem('sintaxis_token');
        localStorage.removeItem('sintaxis_rol');
        localStorage.removeItem('sintaxis_email');
        localStorage.removeItem('sintaxis_idCliente');
        detenerTemporizadorInactividad();
        actualizarUIAuth();
        alert('Sesión cerrada correctamente.');
    }
}

function irPanelAdmin() {
    toggleAdminModal(true);
}

// --- Gestión de Perfil de Cliente y Cambio de Contraseña ---
function toggleMiPerfilModal(abrir) {
    const modal = document.getElementById('miPerfilModal');
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

async function abrirModalMiPerfil() {
    toggleMiPerfilModal(true);
    const container = document.getElementById('miPerfilContainer');
    const token = localStorage.getItem('sintaxis_token');
    const rol = localStorage.getItem('sintaxis_rol');

    if (!token) {
        mostrarToast('Debes iniciar sesión para ver tu perfil.', 'error');
        toggleLoginModal(true);
        return;
    }

    container.innerHTML = `<div style="text-align: center; color: var(--text-secondary); padding: 2rem;">Cargando perfil e historial...</div>`;

    try {
        let data = {};
        let esAdminOEmpleado = rol === 'ROLE_ADMIN' || rol === 'ROLE_EMPLEADO';

        if (esAdminOEmpleado) {
            const respPerfil = await fetch('/api/auth/perfil', {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (!respPerfil.ok) throw new Error('No se pudo cargar el perfil de usuario.');
            const userPerfil = await respPerfil.json();
            
            data = {
                nombre: userPerfil.nombre || 'Administrador',
                apellido: '',
                email: userPerfil.email,
                telefono: userPerfil.telefono || 'No requerido',
                direccion: userPerfil.direccion || 'No requerida',
                esVip: false,
                puntosAcumulados: 0,
                historialCompras: []
            };
        } else {
            const response = await fetch('/api/clientes/mi-perfil', {
                headers: { 'Authorization': 'Bearer ' + token }
            });
            if (!response.ok) throw new Error('No se pudo cargar la información del perfil.');
            data = await response.json();
        }
        
        let historialHtml = '';
        if (!data.historialCompras || data.historialCompras.length === 0) {
            historialHtml = `<div style="text-align: center; color: var(--text-secondary); padding: 1.5rem; font-size: 0.85rem;">No tienes compras registradas todavía.</div>`;
        } else {
            historialHtml = `<div style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 250px; overflow-y: auto; padding-right: 0.25rem;">`;
            data.historialCompras.forEach(v => {
                const fechaF = v.fecha ? new Date(v.fecha).toLocaleString() : 'Fecha no disp.';
                const estadoCol = v.estado === 'Pagada' ? 'color: var(--success-emerald); background: rgba(16,185,129,0.1); border-color: rgba(16,185,129,0.3);' :
                                  v.estado === 'Pendiente' ? 'color: #f59e0b; background: rgba(245,158,11,0.1); border-color: rgba(245,158,11,0.3);' :
                                  'color: var(--danger-rose); background: rgba(244,63,94,0.1); border-color: rgba(244,63,94,0.3);';
                historialHtml += `
                    <div style="background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 0.75rem 1rem; display: flex; justify-content: space-between; align-items: center; gap: 1rem; flex-wrap: wrap;">
                        <div>
                            <div style="font-size: 0.75rem; font-family: 'JetBrains Mono', monospace; color: var(--text-secondary);">PEDIDO #${v.idVenta} — ${fechaF}</div>
                            <div style="font-size: 0.85rem; font-weight: 600; color: var(--text-main); margin-top: 0.1rem;">💳 ${v.metodoPago || 'Efectivo'}</div>
                        </div>
                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                            <span style="font-size: 0.7rem; padding: 0.15rem 0.5rem; border-radius: 4px; border: 1px solid; font-weight: 600; ${estadoCol}">${v.estado}</span>
                            <span style="font-size: 0.95rem; font-weight: 700; font-family: 'JetBrains Mono', monospace; color: var(--accent-cyan);">$${v.total.toFixed(2)}</span>
                        </div>
                    </div>
                `;
            });
            historialHtml += `</div>`;
        }

        container.innerHTML = `
            <div style="display: flex; flex-direction: column; gap: 1.5rem;">
                <!-- Tarjeta de Datos y Puntos -->
                <div style="background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; flex-wrap: wrap; gap: 0.5rem;">
                        <h4 style="font-size: 0.95rem; font-weight: 600; color: var(--text-main); display: flex; align-items: center; gap: 0.4rem;">
                            📋 Datos Personales & Fidelización
                        </h4>
                        <div style="display: flex; gap: 0.5rem; align-items: center;">
                            ${!esAdminOEmpleado ? `
                                <span style="font-size: 0.75rem; background: rgba(56,189,248,0.15); color: var(--accent-cyan); border: 1px solid rgba(56,189,248,0.3); padding: 0.2rem 0.6rem; border-radius: 20px; font-weight: 600;">
                                    🌟 Puntos: ${data.puntosAcumulados || 0}
                                </span>
                                <span style="font-size: 0.75rem; background: ${data.es_vip || data.esVip ? 'rgba(16,185,129,0.2)' : 'rgba(100,116,139,0.2)'}; color: ${data.es_vip || data.esVip ? 'var(--success-emerald)' : 'var(--text-secondary)'}; border: 1px solid ${data.es_vip || data.esVip ? 'rgba(16,185,129,0.4)' : 'rgba(100,116,139,0.3)'}; padding: 0.2rem 0.6rem; border-radius: 20px; font-weight: 600;">
                                    ${data.es_vip || data.esVip ? '👑 VIP MEMBER' : '👤 Cliente Standard'}
                                </span>
                            ` : `
                                <span style="font-size: 0.75rem; background: rgba(16, 185, 129, 0.15); color: var(--success-emerald); border: 1px solid rgba(16, 185, 129, 0.3); padding: 0.2rem 0.6rem; border-radius: 20px; font-weight: 600;">
                                    🛡️ Rol: ${rol.replace('ROLE_', '')}
                                </span>
                            `}
                        </div>
                    </div>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 0.75rem; font-size: 0.85rem;">
                        <div><span style="color: var(--text-secondary);">Nombre:</span> <strong style="color: var(--text-main);">${data.nombre || ''} ${data.apellido || ''}</strong></div>
                        <div><span style="color: var(--text-secondary);">Email:</span> <strong style="color: var(--text-main);">${data.email || ''}</strong></div>
                        ${!esAdminOEmpleado ? `
                            <div><span style="color: var(--text-secondary);">Teléfono:</span> <strong style="color: var(--text-main);">${data.telefono || 'No registrado'}</strong></div>
                            <div><span style="color: var(--text-secondary);">Dirección:</span> <strong style="color: var(--text-main);">${data.direccion || 'No registrada'}</strong></div>
                        ` : ''}
                    </div>
                </div>

                <!-- Historial de Compras (Solo para Clientes) -->
                ${!esAdminOEmpleado ? `
                    <div>
                        <h4 style="font-size: 0.95rem; font-weight: 600; color: var(--text-main); margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.4rem;">
                            🛍️ Historial de Compras (${data.historialCompras ? data.historialCompras.length : 0})
                        </h4>
                        ${historialHtml}
                    </div>
                ` : ''}

                <!-- Sección Cambio de Contraseña -->
                <div style="background: var(--bg-canvas); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem;">
                    <h4 style="font-size: 0.95rem; font-weight: 600; color: var(--text-main); margin-bottom: 0.75rem; display: flex; align-items: center; gap: 0.4rem;">
                        🔐 Seguridad & Cambio de Contraseña
                    </h4>
                    <form id="cambiarPasswordForm" onsubmit="ejecutarCambiarPassword(event)" style="display: flex; flex-direction: column; gap: 0.75rem;">
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 0.75rem;">
                            <div>
                                <label style="display: block; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.3rem;">Contraseña Actual *</label>
                                <input type="password" id="passActual" required autocomplete="current-password" placeholder="••••••••" style="width: 100%; background: var(--surface); border: 1px solid var(--border-color); color: var(--text-main); padding: 0.5rem; border-radius: var(--radius); font-size: 0.85rem;">
                            </div>
                            <div>
                                <label style="display: block; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.3rem;">Nueva Contraseña *</label>
                                <input type="password" id="passNueva" required minlength="6" autocomplete="new-password" placeholder="Mínimo 6 caracteres" style="width: 100%; background: var(--surface); border: 1px solid var(--border-color); color: var(--text-main); padding: 0.5rem; border-radius: var(--radius); font-size: 0.85rem;">
                            </div>
                        </div>
                        <div id="passErrorMsg" style="color: var(--danger-rose); font-size: 0.8rem; display: none;"></div>
                        <div style="display: flex; justify-content: flex-end; margin-top: 0.25rem;">
                            <button type="submit" class="btn btn-primary" style="font-size: 0.8rem; padding: 0.4rem 1rem;">Actualizar Contraseña</button>
                        </div>
                    </form>
                </div>
            </div>
        `;

    } catch (err) {
        console.error(err);
        container.innerHTML = `<div style="text-align: center; color: var(--danger-rose); padding: 2rem;">Error al cargar tu perfil: ${err.message}</div>`;
    }
}

async function ejecutarCambiarPassword(event) {
    event.preventDefault();
    const passwordActual = document.getElementById('passActual').value;
    const passwordNueva = document.getElementById('passNueva').value;
    const errorMsg = document.getElementById('passErrorMsg');
    const token = localStorage.getItem('sintaxis_token');

    errorMsg.style.display = 'none';

    if (passwordNueva.length < 6) {
        errorMsg.textContent = 'La nueva contraseña debe tener al menos 6 caracteres.';
        errorMsg.style.display = 'block';
        return;
    }

    try {
        const response = await fetch('/api/auth/cambiar-password', {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ passwordActual, passwordNueva })
        });

        if (!response.ok) {
            const errData = await response.json().catch(() => ({}));
            throw new Error(errData.message || 'La contraseña actual es incorrecta o el formato es inválido.');
        }

        mostrarToast('¡Contraseña actualizada con éxito!', 'success');
        document.getElementById('cambiarPasswordForm').reset();
    } catch (err) {
        console.error(err);
        errorMsg.textContent = err.message || 'Error al cambiar la contraseña.';
        errorMsg.style.display = 'block';
    }
}

// --- Historial de Compras del Cliente ---
function toggleMisComprasModal(abrir) {
    const modal = document.getElementById('misComprasModal');
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

async function abrirModalMisCompras() {
    toggleMisComprasModal(true);
    const container = document.getElementById('misComprasContainer');
    const token = localStorage.getItem('sintaxis_token');

    if (!token) {
        mostrarToast('Debes iniciar sesión para ver tu historial de compras.', 'error');
        toggleLoginModal(true);
        return;
    }

    container.innerHTML = `<div style="text-align: center; color: var(--text-secondary); padding: 2rem;">Cargando tus compras...</div>`;

    try {
        const response = await fetch('/api/ventas/mis-ventas', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        if (!response.ok) {
            throw new Error('No se pudo cargar el historial de compras.');
        }

        const ventas = await response.json();

        if (!ventas || ventas.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 3rem; color: var(--text-secondary);">
                    <div style="font-size: 2.5rem; margin-bottom: 0.75rem;">🛍️</div>
                    <div style="font-size: 1rem; font-weight: 600; color: var(--text-main); margin-bottom: 0.25rem;">Aún no tienes compras realizadas</div>
                    <p style="font-size: 0.85rem; margin-bottom: 1.5rem;">Explora nuestros celulares y accesorios para realizar tu primer pedido.</p>
                    <button class="btn btn-primary" onclick="toggleMisComprasModal(false)">Ir a Ver Catálogo</button>
                </div>
            `;
            return;
        }

        let html = `<div style="display: flex; flex-direction: column; gap: 1rem;">`;
        ventas.forEach(v => {
            const fechaFormateada = v.fecha ? new Date(v.fecha).toLocaleString() : 'Fecha no disponible';
            const estadoColor = v.estado === 'Pagada' ? 'color: var(--success-emerald); background: rgba(16,185,129,0.1); border-color: rgba(16,185,129,0.3);' :
                                v.estado === 'Pendiente' ? 'color: #f59e0b; background: rgba(245,158,11,0.1); border-color: rgba(245,158,11,0.3);' :
                                v.estado === 'Entregada' ? 'color: var(--accent-cyan); background: rgba(56,189,248,0.1); border-color: rgba(56,189,248,0.3);' :
                                'color: var(--danger-rose); background: rgba(244,63,94,0.1); border-color: rgba(244,63,94,0.3);';

            let detallesHtml = '';
            if (v.detalles && v.detalles.length > 0) {
                detallesHtml = `
                    <div style="margin-top: 0.75rem; border-top: 1px solid var(--border-color); padding-top: 0.75rem;">
                        <div style="font-size: 0.75rem; font-weight: 600; text-transform: uppercase; color: var(--text-secondary); margin-bottom: 0.5rem;">Productos en esta orden:</div>
                        <div style="display: flex; flex-direction: column; gap: 0.4rem;">
                `;
                v.detalles.forEach(d => {
                    const subtotalItem = (d.subtotalDetalle !== undefined && d.subtotalDetalle !== null) ? d.subtotalDetalle : (d.cantidad * (d.precioUnitario || 0));
                    detallesHtml += `
                        <div style="display: flex; justify-content: space-between; font-size: 0.825rem; background: var(--bg-canvas); padding: 0.4rem 0.75rem; border-radius: var(--radius); border: 1px solid var(--border-color);">
                            <span><strong>${d.cantidad}x</strong> ${d.nombreProducto || 'Producto #' + d.idProducto}</span>
                            <span style="font-family: 'JetBrains Mono', monospace; color: var(--accent-cyan);">$${subtotalItem.toFixed(2)}</span>
                        </div>
                    `;
                });
                detallesHtml += `</div></div>`;
            }

            html += `
                <div style="background: var(--surface); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 1.25rem; box-shadow: var(--shadow-kyu);">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 1rem; flex-wrap: wrap;">
                        <div>
                            <div style="font-size: 0.75rem; font-family: 'JetBrains Mono', monospace; color: var(--text-secondary);">PEDIDO #${v.idVenta}</div>
                            <div style="font-size: 0.85rem; color: var(--text-secondary); margin-top: 0.2rem;">📅 ${fechaFormateada}</div>
                            <div style="font-size: 0.85rem; margin-top: 0.3rem;">💳 Método: <strong>${v.metodoPago || 'Efectivo'}</strong></div>
                        </div>
                        <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 0.4rem;">
                            <span style="font-size: 0.75rem; padding: 0.2rem 0.6rem; border-radius: 4px; border: 1px solid; font-weight: 600; ${estadoColor}">${v.estado}</span>
                            <div style="font-size: 1.1rem; font-weight: 700; font-family: 'JetBrains Mono', monospace; color: var(--accent-cyan);">$${v.total.toFixed(2)}</div>
                        </div>
                    </div>
                    ${detallesHtml}
                </div>
            `;
        });
        html += `</div>`;
        container.innerHTML = html;

    } catch (err) {
        console.error(err);
        container.innerHTML = `<div style="text-align: center; color: var(--danger-rose); padding: 2rem;">Error al cargar tus compras: ${err.message}</div>`;
    }
}

function mostrarToast(mensaje, tipo = 'success') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast-notification ${tipo}`;

    let icono = '✨';
    if (tipo === 'success') icono = '✅';
    if (tipo === 'error') icono = '❌';
    if (tipo === 'info') icono = 'ℹ️';

    toast.innerHTML = `
        <span style="font-size: 1.1rem; flex-shrink: 0;">${icono}</span>
        <div style="flex: 1; font-weight: 500;">${mensaje}</div>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('hiding');
        setTimeout(() => {
            toast.remove();
        }, 250);
    }, 3500);
}

// --- Registro Público & Gestión de Usuarios ---
function toggleRegisterModal(abrir) {
    const modal = document.getElementById('registerModal');
    const errorMsg = document.getElementById('registerErrorMsg');
    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

function abrirModalRegistroPublico() {
    toggleLoginModal(false);
    const form = document.getElementById('registerForm');
    if (form) form.reset();
    toggleRegisterModal(true);
}

async function ejecutarRegistroPublico(event) {
    event.preventDefault();
    const errorMsg = document.getElementById('registerErrorMsg');
    errorMsg.style.display = 'none';

    const nombre = document.getElementById('regNombre').value.trim();
    const apellido = document.getElementById('regApellido').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value;
    const telefono = document.getElementById('regTelefono').value.trim();
    const direccion = document.getElementById('regDireccion').value.trim();

    const dto = {
        nombre,
        apellido,
        email,
        password,
        rol: 'CLIENTE',
        telefono,
        direccion
    };

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || 'Error al registrar la cuenta.');
        }

        const data = await res.json();
        let rolGuardar = data.rol;
        if (rolGuardar && !rolGuardar.startsWith('ROLE_')) {
            rolGuardar = 'ROLE_' + rolGuardar;
        }
        localStorage.setItem('sintaxis_token', data.token);
        localStorage.setItem('sintaxis_rol', rolGuardar);
        localStorage.setItem('sintaxis_email', data.email);

        toggleRegisterModal(false);
        actualizarUIAuth();
        alert(`¡Cuenta creada con éxito! Bienvenido, ${data.email}`);
    } catch (err) {
        console.error(err);
        errorMsg.textContent = err.message || 'Error al registrarse.';
        errorMsg.style.display = 'block';
    }
}

let usuariosGlobal = [];

function toggleUserFormModal(abrir) {
    const modal = document.getElementById('userFormModal');
    const errorMsg = document.getElementById('userFormErrorMsg');
    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

function abrirModalNuevoUsuarioAdmin() {
    abrirModalCrearUsuario();
}

async function cargarUsuariosAdmin() {
    const token = localStorage.getItem('sintaxis_token');
    const tbody = document.getElementById('adminUserTableBody');
    if (!tbody) return;

    if (!token) {
        alert('Sesión expirada o no iniciada.');
        localStorage.removeItem('sintaxis_token');
        localStorage.removeItem('sintaxis_rol');
        localStorage.removeItem('sintaxis_email');
        toggleAdminModal(false);
        actualizarUIAuth();
        toggleLoginModal(true);
        return;
    }

    tbody.innerHTML = `<tr><td colspan="5" class="loading">Cargando usuarios desde /api/auth/usuarios...</td></tr>`;

    try {
        const res = await fetch('/api/auth/usuarios', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (res.status === 401 || res.status === 403) {
            localStorage.removeItem('sintaxis_token');
            localStorage.removeItem('sintaxis_rol');
            localStorage.removeItem('sintaxis_email');
            actualizarUIAuth();
            toggleAdminModal(false);
            alert('Su sesión ha expirado o no tiene permisos de Administrador. Por favor, vuelva a iniciar sesión.');
            toggleLoginModal(true);
            return;
        }

        if (!res.ok) {
            throw new Error('Error al cargar la lista de usuarios. Status: ' + res.status);
        }

        usuariosGlobal = await res.json();
        renderizarTablaUsuarios(usuariosGlobal);
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state" style="color: var(--danger-rose);">Error al cargar usuarios: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderizarTablaUsuarios(usuarios) {
    const tbody = document.getElementById('adminUserTableBody');
    if (!tbody) return;

    if (usuarios.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state" style="padding: 3rem 1rem;">No hay usuarios registrados.</td></tr>`;
        return;
    }

    tbody.innerHTML = usuarios.map(u => {
        let badgeRol = 'badge-normal';
        if (u.rol === 'ROLE_ADMIN' || u.rol === 'ADMIN') badgeRol = 'badge-warning';
        else if (u.rol === 'ROLE_EMPLEADO' || u.rol === 'EMPLEADO') badgeRol = 'badge-active';

        return `
            <tr>
                <td style="padding: 0.8rem 1rem;">
                    <div class="product-title" style="font-size: 0.85rem;">${escapeHtml(u.nombre)}</div>
                    <div style="font-size: 0.70rem; color: var(--text-muted); font-family: 'JetBrains Mono', monospace;">ID: #${u.idUsuario}</div>
                </td>
                <td style="padding: 0.8rem 1rem; color: var(--text-secondary); font-size: 0.85rem;">
                    ${escapeHtml(u.email)}
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="badge ${badgeRol}">${u.rol}</span>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    ${u.activo !== false ? '<span class="badge badge-active">Activo</span>' : '<span class="badge badge-inactive">Inactivo</span>'}
                </td>
                <td style="padding: 0.8rem 1rem; text-align: right; display: flex; gap: 0.5rem; justify-content: flex-end;">
                    <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem; border-color: var(--accent-cyan); color: var(--accent-cyan);" onclick="abrirModalEditarUsuario(${u.idUsuario})">✏️ Editar</button>
                    <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem; border-color: var(--danger-rose); color: var(--danger-rose);" onclick="eliminarUsuarioAdmin(${u.idUsuario}, '${escapeHtml(u.email)}')">🗑️ Eliminar</button>
                </td>
            </tr>
        `;
    }).join('');
}

let usuarioEnEdicionId = null;

function abrirModalCrearUsuario() {
    usuarioEnEdicionId = null;
    const modal = document.getElementById('userFormModal');
    const title = document.getElementById('userModalTitle');
    const btnSubmit = document.getElementById('userModalSubmitBtn');
    const form = document.getElementById('userForm');
    const errorMsg = document.getElementById('userFormErrorMsg');
    
    if (title) title.textContent = '➕ Registrar Nuevo Usuario (Admin)';
    if (btnSubmit) btnSubmit.textContent = 'Crear Usuario';
    if (errorMsg) errorMsg.style.display = 'none';
    if (form) form.reset();
    
    const passInput = document.getElementById('adminUserPassword');
    if (passInput) {
        passInput.required = true;
        passInput.placeholder = '••••••••';
    }

    if (modal) modal.style.display = 'flex';
}

async function abrirModalEditarUsuario(idUsuario) {
    usuarioEnEdicionId = idUsuario;
    const token = localStorage.getItem('sintaxis_token');
    const modal = document.getElementById('userFormModal');
    const title = document.getElementById('userModalTitle');
    const btnSubmit = document.getElementById('userModalSubmitBtn');
    const errorMsg = document.getElementById('userFormErrorMsg');
    
    if (title) title.textContent = '✏️ Editar Usuario (Admin)';
    if (btnSubmit) btnSubmit.textContent = 'Guardar Cambios';
    if (errorMsg) errorMsg.style.display = 'none';

    try {
        const res = await fetch(`/api/auth/usuarios/${idUsuario}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) throw new Error('No se pudo obtener la información del usuario.');
        const u = await res.json();

        document.getElementById('adminUserNombre').value = u.nombre || '';
        document.getElementById('adminUserApellido').value = ''; 
        document.getElementById('adminUserEmail').value = u.email || '';
        document.getElementById('adminUserRol').value = u.rol.replace('ROLE_', '');
        
        const passInput = document.getElementById('adminUserPassword');
        if (passInput) {
            passInput.value = '';
            passInput.required = false;
            passInput.placeholder = 'Dejar en blanco para mantener contraseña';
        }

        if (modal) modal.style.display = 'flex';
    } catch (err) {
        console.error(err);
        alert('Error al cargar datos del usuario para editar.');
    }
}

async function guardarUsuarioAdmin(event) {
    event.preventDefault();
    const token = localStorage.getItem('sintaxis_token');
    const errorMsg = document.getElementById('userFormErrorMsg');
    errorMsg.style.display = 'none';

    const nombre = document.getElementById('adminUserNombre').value.trim();
    const apellido = document.getElementById('adminUserApellido').value.trim();
    const email = document.getElementById('adminUserEmail').value.trim();
    const password = document.getElementById('adminUserPassword').value;
    const rol = document.getElementById('adminUserRol').value;

    try {
        if (usuarioEnEdicionId) {
            // Modo Edición (PUT)
            const dto = { nombre, email, password: password ? password : null, rol, activo: true };
            const res = await fetch(`/api/auth/usuarios/${usuarioEnEdicionId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const errJson = await res.json().catch(() => ({}));
                throw new Error(errJson.message || 'Error al actualizar el usuario.');
            }

            alert('¡Usuario actualizado con éxito!');
        } else {
            // Modo Creación (POST)
            const dto = { nombre, apellido, email, password, rol };
            const res = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(dto)
            });

            if (!res.ok) {
                const errJson = await res.json().catch(() => ({}));
                throw new Error(errJson.message || 'Error al crear el usuario.');
            }

            alert(`¡Usuario ${rol} creado con éxito!`);
        }

        toggleUserFormModal(false);
        cargarUsuariosAdmin();
    } catch (err) {
        console.error(err);
        errorMsg.textContent = err.message || 'Error al guardar usuario.';
        errorMsg.style.display = 'block';
    }
}

async function eliminarUsuarioAdmin(idUsuario, emailUsuario) {
    const adminEmail = localStorage.getItem('sintaxis_email');
    if (adminEmail && adminEmail.toLowerCase() === emailUsuario.toLowerCase()) {
        alert('Acción no permitida: No puedes eliminar tu propia cuenta de administrador.');
        return;
    }

    if (!confirm(`¿Estás seguro de que deseas eliminar al usuario #${idUsuario} (${emailUsuario})?`)) {
        return;
    }

    const token = localStorage.getItem('sintaxis_token');
    try {
        const res = await fetch(`/api/auth/usuarios/${idUsuario}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || 'Error al eliminar el usuario.');
        }

        alert('Usuario eliminado correctamente.');
        cargarUsuariosAdmin();
    } catch (err) {
        console.error(err);
        alert('Error: ' + err.message);
    }
}

// --- Panel Admin & CRUD con JWT ---
let adminTabActiva = 'todos';

/**
 * Verificación real de la sesión contra el backend. Nunca se confía únicamente
 * en el rol guardado en localStorage (que puede ser manipulado desde el navegador).
 */
async function verificarAccesoAdminServidor(token) {
    try {
        const res = await fetch('/api/auth/perfil', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) return { autorizado: false, status: res.status };

        const perfil = await res.json();
        const rol = String(perfil.rol || '').replace(/^ROLE_/, '');
        return {
            autorizado: rol === 'ADMIN' || rol === 'EMPLEADO',
            rol,
            status: res.status,
            perfil
        };
    } catch (err) {
        console.error('No se pudo verificar la sesión contra el servidor', err);
        return { autorizado: false, status: 0 };
    }
}

function limpiarSesionLocal() {
    ['sintaxis_token', 'sintaxis_rol', 'sintaxis_email', 'sintaxis_idCliente'].forEach(k => localStorage.removeItem(k));
    detenerTemporizadorInactividad();
    actualizarUIAuth();
}

async function toggleAdminModal(abrir) {
    const token = localStorage.getItem('sintaxis_token');
    const rolLocal = localStorage.getItem('sintaxis_rol');

    if (abrir) {
        if (!token || (rolLocal !== 'ROLE_ADMIN' && rolLocal !== 'ROLE_EMPLEADO' && rolLocal !== 'ADMIN' && rolLocal !== 'EMPLEADO')) {
            mostrarToast('Acceso denegado: no tenés permisos de Administrador o Empleado.', 'error');
            return;
        }

        const verificacion = await verificarAccesoAdminServidor(token);
        if (!verificacion.autorizado) {
            if (verificacion.status === 401 || verificacion.status === 403) {
                limpiarSesionLocal();
                mostrarToast('Tu sesión expiró o no tenés permisos. Iniciá sesión nuevamente.', 'error');
            } else {
                mostrarToast('No se pudo verificar la sesión con el servidor. Intentá nuevamente.', 'error');
            }
            return;
        }

        // El rol autoritativo es el que devuelve el backend.
        localStorage.setItem('sintaxis_rol', 'ROLE_' + verificacion.rol);
    }

    const modal = document.getElementById('adminModal');
    if (abrir) {
        modal.style.display = 'flex';
        actualizarContadorStockCritico();
        cambiarAdminTab('todos');
    } else {
        modal.style.display = 'none';
    }
}

function cambiarAdminTab(tab) {
    adminTabActiva = tab;
    const prodSection = document.getElementById('adminProductsSection');
    const userSection = document.getElementById('adminUsersSection');
    const ventasSection = document.getElementById('adminVentasSection');
    const clientesSection = document.getElementById('adminClientesSection');
    const searchInput = document.getElementById('adminSearchInput');

    ['todos', 'celulares', 'accesorios', 'stock-critico', 'usuarios', 'clientes', 'subcategorias', 'ventas'].forEach(t => {
        const btn = document.getElementById(`admin-tab-${t}`);
        if (btn) {
            if (t === tab) {
                btn.style.background = t === 'stock-critico' ? 'rgba(244,63,94,0.2)' : 'var(--accent-cyan)';
                btn.style.color = t === 'stock-critico' ? 'var(--danger-rose)' : '#090A0F';
                btn.style.fontWeight = '600';
                btn.style.border = t === 'stock-critico' ? '1px solid var(--danger-rose)' : 'none';
            } else {
                btn.style.background = 'transparent';
                btn.style.color = t === 'stock-critico' ? 'var(--danger-rose)' : 'var(--text-secondary)';
                btn.style.fontWeight = '400';
                btn.style.border = t === 'stock-critico' ? '1px solid rgba(244,63,94,0.3)' : '1px solid var(--border-color)';
            }
        }
    });

    const headerActionsContainer = document.getElementById('adminHeaderActions');
    const existingBtn = document.getElementById('btnAdminAccionPrincipal');
    if (existingBtn) existingBtn.remove();

    if (tab === 'usuarios') {
        if (prodSection) prodSection.style.display = 'none';
        if (userSection) userSection.style.display = 'block';
        const subcatSection = document.getElementById('adminSubcategoriasSection');
        if (subcatSection) subcatSection.style.display = 'none';
        if (ventasSection) ventasSection.style.display = 'none';
        if (clientesSection) clientesSection.style.display = 'none';
        if (searchInput) searchInput.style.display = 'none';
        
        if (headerActionsContainer) {
            const newBtn = document.createElement('button');
            newBtn.id = 'btnAdminAccionPrincipal';
            newBtn.className = 'btn btn-primary';
            newBtn.innerHTML = '➕ Nuevo Usuario';
            newBtn.onclick = abrirModalNuevoUsuarioAdmin;
            headerActionsContainer.prepend(newBtn);
        }

        cargarUsuariosAdmin();
    } else if (tab === 'clientes') {
        if (prodSection) prodSection.style.display = 'none';
        if (userSection) userSection.style.display = 'none';
        const subcatSection = document.getElementById('adminSubcategoriasSection');
        if (subcatSection) subcatSection.style.display = 'none';
        if (ventasSection) ventasSection.style.display = 'none';
        if (clientesSection) clientesSection.style.display = 'block';
        if (searchInput) searchInput.style.display = 'none';

        cargarClientesAdmin();
    } else if (tab === 'subcategorias') {
        if (prodSection) prodSection.style.display = 'none';
        if (userSection) userSection.style.display = 'none';
        const subcatSection = document.getElementById('adminSubcategoriasSection');
        if (subcatSection) subcatSection.style.display = 'flex';
        if (ventasSection) ventasSection.style.display = 'none';
        if (clientesSection) clientesSection.style.display = 'none';
        if (searchInput) searchInput.style.display = 'none';

        if (headerActionsContainer) {
            const newBtn = document.createElement('button');
            newBtn.id = 'btnAdminAccionPrincipal';
            newBtn.className = 'btn btn-primary';
            newBtn.style.background = 'rgba(56, 189, 248, 0.15)';
            newBtn.style.color = 'var(--accent-cyan)';
            newBtn.style.borderColor = 'rgba(56, 189, 248, 0.3)';
            newBtn.innerHTML = '+ Nueva Subcategoría';
            newBtn.onclick = abrirModalNuevaSubcategoria;
            headerActionsContainer.prepend(newBtn);
        }

        cargarSubcategoriasAdmin();
    } else if (tab === 'ventas') {
        if (prodSection) prodSection.style.display = 'none';
        if (userSection) userSection.style.display = 'none';
        const subcatSection = document.getElementById('adminSubcategoriasSection');
        if (subcatSection) subcatSection.style.display = 'none';
        if (ventasSection) ventasSection.style.display = 'flex';
        if (clientesSection) clientesSection.style.display = 'none';
        if (searchInput) {
            searchInput.style.display = 'none';
            searchInput.value = ''; // limpiar búsqueda al cambiar de pestaña
        }

        cargarVentasAdmin();
    } else {
        if (prodSection) prodSection.style.display = 'block';
        if (userSection) userSection.style.display = 'none';
        const subcatSection = document.getElementById('adminSubcategoriasSection');
        if (subcatSection) subcatSection.style.display = 'none';
        if (ventasSection) ventasSection.style.display = 'none';
        if (clientesSection) clientesSection.style.display = 'none';
        if (searchInput) searchInput.style.display = 'block';

        if (headerActionsContainer) {
            const newBtn = document.createElement('button');
            newBtn.id = 'btnAdminAccionPrincipal';
            newBtn.className = 'btn btn-primary';
            newBtn.innerHTML = '➕ Nuevo Producto';
            newBtn.onclick = () => abrirModalFormProducto('crear');
            headerActionsContainer.prepend(newBtn);
        }

        filtrarProductosAdmin();
    }
}

let clientesGlobal = [];

async function cargarClientesAdmin() {
    const token = localStorage.getItem('sintaxis_token');
    const tbody = document.getElementById('adminClienteTableBody');
    if (!tbody) return;

    if (!token) {
        alert('Sesión expirada o no iniciada.');
        toggleAdminModal(false);
        toggleLoginModal(true);
        return;
    }

    tbody.innerHTML = `<tr><td colspan="5" class="loading">Cargando clientes desde /api/clientes...</td></tr>`;

    try {
        const res = await fetch('/api/clientes', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (res.status === 401 || res.status === 403) {
            localStorage.removeItem('sintaxis_token');
            localStorage.removeItem('sintaxis_rol');
            localStorage.removeItem('sintaxis_email');
            actualizarUIAuth();
            toggleAdminModal(false);
            alert('No tiene permisos para ver el listado de clientes.');
            toggleLoginModal(true);
            return;
        }

        if (!res.ok) {
            throw new Error('Error al cargar clientes. Status: ' + res.status);
        }

        const jsonC = await res.json();
        clientesGlobal = Array.isArray(jsonC) ? jsonC : (jsonC.content || []);
        renderizarTablaClientes(clientesGlobal);
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state" style="color: var(--danger-rose);">Error al cargar clientes: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderizarTablaClientes(clientes) {
    const tbody = document.getElementById('adminClienteTableBody');
    if (!tbody) return;

    if (clientes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="empty-state" style="padding: 3rem 1rem;">No hay clientes registrados en el sistema.</td></tr>`;
        return;
    }

    tbody.innerHTML = clientes.map(c => {
        const nombreCompleto = `${c.nombre || ''} ${c.apellido || ''}`.trim() || 'Sin Nombre';
        const email = c.email || 'Sin email';
        const telefono = c.telefono || 'Sin tel.';
        const direccion = c.direccion || 'Sin dir.';
        const idCliente = c.idCliente || c.id;
        const puntos = c.puntosAcumulados !== undefined ? c.puntosAcumulados : 0;

        return `
            <tr>
                <td style="padding: 0.8rem 1rem;">
                    <div class="product-title" style="font-size: 0.85rem;">${escapeHtml(nombreCompleto)}</div>
                    <div style="font-size: 0.70rem; color: var(--text-muted); font-family: 'JetBrains Mono', monospace;">ID: #${idCliente}</div>
                </td>
                <td style="padding: 0.8rem 1rem; color: var(--text-secondary); font-size: 0.85rem;">
                    ${escapeHtml(email)}
                </td>
                <td style="padding: 0.8rem 1rem; color: var(--text-main); font-size: 0.85rem;">
                    ${escapeHtml(telefono)}
                </td>
                <td style="padding: 0.8rem 1rem; color: var(--text-secondary); font-size: 0.8rem;">
                    ${escapeHtml(direccion)}
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="badge badge-active">Activo</span>
                </td>
                <td style="padding: 0.8rem 1rem; font-family: 'JetBrains Mono', monospace; color: var(--accent-cyan); font-size: 0.85rem;">
                    🌟 ${puntos} pts
                </td>
                <td style="padding: 0.8rem 1rem; text-align: right; display: flex; gap: 0.5rem; justify-content: flex-end; align-items: center;">
                    <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem;" onclick='abrirEditarCliente(${JSON.stringify(c)})'>✏️ Editar</button>
                    <button class="btn btn-secondary" style="padding: 0.3rem 0.6rem; font-size: 0.75rem; border-color: var(--danger-rose); color: var(--danger-rose);" onclick="eliminarClienteAdmin(${idCliente}, '${escapeHtml(nombreCompleto)}')">🗑️ Eliminar</button>
                </td>
            </tr>
        `;
    }).join('');
}

function toggleClienteFormModal(show) {
    const modal = document.getElementById('clienteFormModal');
    const errorMsg = document.getElementById('clienteFormErrorMsg');
    if (modal) {
        modal.style.display = show ? 'flex' : 'none';
        if (!show && errorMsg) {
            errorMsg.style.display = 'none';
        }
    }
}

function abrirEditarCliente(cliente) {
    const id = cliente.idCliente || cliente.id;
    document.getElementById('formClienteId').value = id;
    document.getElementById('formClienteNombre').value = cliente.nombre || '';
    document.getElementById('formClienteApellido').value = cliente.apellido || '';
    document.getElementById('formClienteEmail').value = cliente.email || '';
    document.getElementById('formClienteTelefono').value = cliente.telefono || '';
    document.getElementById('formClienteDireccion').value = cliente.direccion || '';

    toggleClienteFormModal(true);
}

async function guardarClienteAdmin(event) {
    event.preventDefault();
    const token = localStorage.getItem('sintaxis_token');
    const errorMsg = document.getElementById('clienteFormErrorMsg');
    if (errorMsg) errorMsg.style.display = 'none';

    const id = document.getElementById('formClienteId').value;
    const nombre = document.getElementById('formClienteNombre').value.trim();
    const apellido = document.getElementById('formClienteApellido').value.trim();
    const email = document.getElementById('formClienteEmail').value.trim();
    const telefono = document.getElementById('formClienteTelefono').value.trim();
    const direccion = document.getElementById('formClienteDireccion').value.trim();

    const dto = {
        nombre,
        apellido,
        email,
        telefono,
        direccion
    };

    try {
        let url = '/api/clientes';
        let method = 'POST';

        if (id) {
            url = `/api/clientes/${id}`;
            method = 'PUT';
        }

        const res = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'Error al guardar el cliente.');
        }

        toggleClienteFormModal(false);
        alert(id ? '✅ ¡Cliente actualizado exitosamente!' : '✅ ¡Cliente creado exitosamente!');
        cargarClientesAdmin();
    } catch (err) {
        console.error(err);
        if (errorMsg) {
            errorMsg.textContent = err.message;
            errorMsg.style.display = 'block';
        } else {
            alert('Error: ' + err.message);
        }
    }
}

async function eliminarClienteAdmin(idCliente, nombreCliente) {
    if (!confirm(`¿Estás seguro de que deseas dar de baja (borrado lógico) al cliente #${idCliente} (${nombreCliente})?`)) {
        return;
    }

    const token = localStorage.getItem('sintaxis_token');
    try {
        const res = await fetch(`/api/clientes/${idCliente}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            throw new Error(errData.message || 'Error al eliminar el cliente.');
        }

        alert('Cliente dado de baja correctamente.');
        cargarClientesAdmin();
    } catch (err) {
        console.error(err);
        alert('Error: ' + err.message);
    }
}

let ventasGlobal = [];

async function cargarVentasAdmin() {
    const token = localStorage.getItem('sintaxis_token');
    const tbody = document.getElementById('adminVentasTableBody');
    if (!tbody) return;

    if (!token) {
        alert('Sesión expirada o no iniciada.');
        toggleAdminModal(false);
        toggleLoginModal(true);
        return;
    }

    tbody.innerHTML = `<tr><td colspan="6" class="loading">Cargando historial de ventas desde /api/ventas...</td></tr>`;

    try {
        console.log('Token enviado a ventas:', token);
        const res = await fetch('/api/ventas', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (res.status === 401 || res.status === 403) {
            const rolActual = localStorage.getItem('sintaxis_rol');
            if (rolActual === 'ROLE_CLIENTE' || rolActual === 'CLIENTE') {
                toggleAdminModal(false);
                alert('La sección de gestión de ventas está reservada para Administradores y Empleados.');
                return;
            }
            localStorage.removeItem('sintaxis_token');
            localStorage.removeItem('sintaxis_rol');
            localStorage.removeItem('sintaxis_email');
            actualizarUIAuth();
            toggleAdminModal(false);
            alert('No tiene permisos de Administrador o Empleado para ver el reporte de ventas.');
            toggleLoginModal(true);
            return;
        }

        if (!res.ok) {
            throw new Error('Error al cargar ventas. Status: ' + res.status);
        }

        const jsonV = await res.json();
        ventasGlobal = Array.isArray(jsonV) ? jsonV : (jsonV.content || []);
        renderizarTablaVentas(ventasGlobal);
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state" style="color: var(--danger-rose);">Error al cargar ventas: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderizarTablaVentas(ventas) {
    const tbody = document.getElementById('adminVentasTableBody');
    const totalIngresosEl = document.getElementById('adminVentasTotalIngresos');
    const totalCountEl = document.getElementById('adminVentasTotalCount');
    if (!tbody) return;

    totalCountEl.textContent = ventas.length;
    
    const totalIngresos = ventas
        .filter(v => v.estado === 'Pagada' || v.estado === 'Entregada')
        .reduce((sum, v) => sum + (v.total || 0), 0);
    
    totalIngresosEl.textContent = '$' + totalIngresos.toLocaleString('es-ES', { minimumFractionDigits: 2 });

    if (ventas.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state" style="padding: 3rem 1rem;">No hay ventas registradas en el sistema.</td></tr>`;
        return;
    }

    tbody.innerHTML = ventas.map(v => {
        let badgeEstado = 'badge-normal';
        if (v.estado === 'Pagada' || v.estado === 'Entregada') badgeEstado = 'badge-active';
        else if (v.estado === 'Pendiente') badgeEstado = 'badge-warning';
        else if (v.estado === 'Cancelada') badgeEstado = 'badge-inactive';

        const fechaFormateada = v.fecha ? new Date(v.fecha).toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' }) : 'Fecha no disp.';
        const clienteNombre = v.clienteNombre || v.clienteEmail || `Cliente #${v.idCliente || 'N/A'}`;

        return `
            <tr>
                <td style="padding: 0.8rem 1rem; font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent-cyan);">
                    #${v.idVenta}
                </td>
                <td style="padding: 0.8rem 1rem; color: var(--text-secondary); font-size: 0.8rem;">
                    ${fechaFormateada}
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <div style="font-weight: 500;">${escapeHtml(clienteNombre)}</div>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="mono" style="font-weight: 600; color: var(--text-main);">$${Number(v.total || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 })}</span>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="badge ${badgeEstado}">${v.estado}</span>
                </td>
                <td style="padding: 0.8rem 1rem; text-align: right;">
                    <select onchange="cambiarEstadoVentaAdmin(${v.idVenta}, this.value)" style="background: var(--bg-canvas); border: 1px solid var(--border-color); color: var(--text-main); padding: 0.3rem 0.5rem; border-radius: var(--radius); font-size: 0.75rem;">
                        <option value="" disabled selected>Cambiar estado...</option>
                        <option value="Pendiente">Pendiente</option>
                        <option value="Pagada">Pagada</option>
                        <option value="Entregada">Entregada</option>
                        <option value="Cancelada">Cancelada</option>
                    </select>
                </td>
            </tr>
        `;
    }).join('');
}

async function cambiarEstadoVentaAdmin(idVenta, nuevoEstado) {
    const token = localStorage.getItem('sintaxis_token');
    if (!token) {
        alert('Sesión expirada.');
        return;
    }

    if (!confirm(`¿Desea cambiar el estado de la venta #${idVenta} a "${nuevoEstado}"?`)) {
        cargarVentasAdmin();
        return;
    }

    try {
        const res = await fetch(`/api/ventas/${idVenta}/estado?estado=${encodeURIComponent(nuevoEstado)}`, {
            method: 'PATCH',
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || 'No se pudo actualizar el estado de la venta.');
        }

        alert(`¡Venta #${idVenta} actualizada a "${nuevoEstado}" con éxito!`);
        cargarVentasAdmin();
    } catch (err) {
        console.error(err);
        alert(err.message || 'Error al cambiar estado de venta.');
        cargarVentasAdmin();
    }
}

function cargarProductosAdmin() {
    actualizarContadorStockCritico();
    filtrarProductosAdmin();
}

function filtrarProductosAdmin() {
    let fuente = [];
    if (adminTabActiva === 'todos') {
        fuente = [...celularesGlobal, ...accesoriosGlobal];
    } else if (adminTabActiva === 'celulares') {
        fuente = celularesGlobal;
    } else if (adminTabActiva === 'accesorios') {
        fuente = accesoriosGlobal;
    } else if (adminTabActiva === 'stock-critico') {
        fuente = [...celularesGlobal, ...accesoriosGlobal].filter(p => p.stock <= p.stockMinimo);
    }

    const query = (document.getElementById('adminSearchInput')?.value || '').trim().toLowerCase();

    const filtrados = fuente.filter(p => {
        return !query ||
            (p.nombre && p.nombre.toLowerCase().includes(query)) ||
            (p.marca && p.marca.toLowerCase().includes(query)) ||
            (p.descripcion && p.descripcion.toLowerCase().includes(query)) ||
            (p.modelo && p.modelo.toLowerCase().includes(query));
    });

    renderizarTablaAdmin(filtrados);
}

function renderizarTablaAdmin(productos) {
    const tbody = document.getElementById('adminProductTableBody');
    if (!tbody) return;

    if (productos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="empty-state" style="padding: 3rem 1rem;">
            <strong>No hay productos registrados o coincidentes.</strong>
        </td></tr>`;
        return;
    }

    tbody.innerHTML = productos.map(p => {
        const idProd = p.idProducto || p.id;
        const tipo = p._tipo;
        const imagenDefault = p.imagenUrl && p.imagenUrl.trim() !== ''
            ? (p.imagenUrl.startsWith('http://localhost:8080/api/productos/') 
                ? p.imagenUrl.replace('/api/productos/', '/api/imagenes/') 
                : p.imagenUrl)
            : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=100&auto=format&fit=crop&q=60';
        const esStockBajo = p.stock <= p.stockMinimo;

        return `
            <tr>
                <td style="padding: 0.8rem 1rem;">
                    <div class="product-cell">
                        <img src="${imagenDefault}" alt="${p.nombre}" class="product-img" style="width: 36px; height: 36px;">
                        <div>
                            <div class="product-title" style="font-size: 0.85rem;">${escapeHtml(p.nombre)}</div>
                            <div style="font-size: 0.70rem; color: var(--text-muted); font-family: 'JetBrains Mono', monospace;">ID: #${idProd}</div>
                        </div>
                    </div>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="badge badge-normal" style="font-size: 0.7rem;">${tipo === 'celular' ? 'Celular' : 'Accesorio'}</span>
                    <div style="font-size: 0.75rem; color: var(--text-secondary); margin-top: 0.1rem;">${escapeHtml(p.marca || 'Genérica')}</div>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span style="font-size: 0.85rem; color: var(--accent-cyan); font-weight: 500;">${tipo === 'accesorio' ? escapeHtml(p.subcategoriaNombre || p.subcategoria || 'N/A') : '-'}</span>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="mono" style="font-weight: 600; color: var(--accent-cyan);">$${Number(p.precio || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 })}</span>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    <span class="mono ${esStockBajo ? 'badge badge-warning' : ''}" style="${esStockBajo ? '' : 'font-weight: 500;'}">
                        ${p.stock} un.
                    </span>
                </td>
                <td style="padding: 0.8rem 1rem;">
                    ${p.activo !== false ? '<span class="badge badge-active">Activo</span>' : '<span class="badge badge-inactive">Inactivo</span>'}
                </td>
                <td style="padding: 0.8rem 1rem; text-align: right;">
                    <div style="display: flex; gap: 0.35rem; justify-content: flex-end;">
                        <button class="btn btn-secondary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem; color: var(--accent-cyan); border-color: rgba(6,182,212,0.3);" onclick="abrirModalAgregarStock(${idProd}, '${tipo}', '${escapeHtml(p.nombre).replace(/'/g, "\\'")}', ${p.stock})">
                            📦 + Stock
                        </button>
                        <button class="btn btn-secondary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem;" onclick="prepararEditarProducto(${idProd}, '${tipo}')">
                            ✏️ Editar
                        </button>
                        <button class="btn btn-secondary" style="padding: 0.25rem 0.6rem; font-size: 0.75rem; color: var(--danger-rose); border-color: rgba(244,63,94,0.3);" onclick="eliminarProductoAdmin(${idProd}, '${tipo}', '${escapeHtml(p.nombre)}')">
                            🗑️ Eliminar
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function toggleProductFormModal(abrir) {
    const modal = document.getElementById('productFormModal');
    const errorMsg = document.getElementById('productFormErrorMsg');
    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

function actualizarCamposTipoForm() {
    const tipo = document.getElementById('formTipoProducto').value;
    const camposCelular = document.getElementById('camposCelularGroup');
    const camposAccesorio = document.getElementById('camposAccesorioGroup');

    if (tipo === 'celular') {
        camposCelular.style.display = 'flex';
        camposAccesorio.style.display = 'none';
    } else {
        camposCelular.style.display = 'none';
        camposAccesorio.style.display = 'flex';
    }
}

function abrirModalFormProducto(modo, producto = null) {
    cargarSubcategorias();
    const form = document.getElementById('productForm');
    if (form) form.reset();

    const title = document.getElementById('productFormTitle');
    const selectTipo = document.getElementById('formTipoProducto');
    const btnSubmit = document.getElementById('btnSubmitForm');
    const idInput = document.getElementById('formProductId');

    if (modo === 'crear') {
        title.textContent = '➕ Registrar Nuevo Producto';
        btnSubmit.textContent = 'Guardar Producto';
        idInput.value = '';
        selectTipo.disabled = false;
        selectTipo.value = 'celular';
    } else if (modo === 'editar' && producto) {
        title.textContent = `✏️ Editar Producto #${producto.idProducto || producto.id}`;
        btnSubmit.textContent = 'Actualizar Producto';
        idInput.value = producto.idProducto || producto.id;
        
        selectTipo.value = producto._tipo || 'celular';
        selectTipo.disabled = true;

        document.getElementById('formNombre').value = producto.nombre || '';
        document.getElementById('formMarca').value = producto.marca || '';
        document.getElementById('formDescripcion').value = producto.descripcion || '';
        document.getElementById('formPrecio').value = producto.precio || 0;
        document.getElementById('formStock').value = producto.stock || 0;
        document.getElementById('formStockMinimo').value = producto.stockMinimo !== undefined ? producto.stockMinimo : 5;
        document.getElementById('formImagenUrl').value = producto.imagenUrl || '';

        if (producto._tipo === 'celular') {
            document.getElementById('formModelo').value = producto.modelo || '';
            document.getElementById('formSistemaOperativo').value = producto.sistemaOperativo || '';
            document.getElementById('formAlmacenamientoGB').value = producto.almacenamientoGB || 128;
            document.getElementById('formRamGB').value = producto.ramGB || 8;
            document.getElementById('formProcesador').value = producto.procesador || '';
            document.getElementById('formPantallaPulgadas').value = producto.pantallaPulgadas || 6.5;
            document.getElementById('formBateriaMAh').value = producto.bateriaMAh || 5000;
            document.getElementById('formColorCelular').value = producto.color || '';
            document.getElementById('formEsLibre').checked = producto.esLibre !== false;
        } else if (producto._tipo === 'accesorio') {
            document.getElementById('formSubcategoriaAccesorio').value = producto.subcategoriaId || '';
            const inputMaterial = document.getElementById('formMaterial');
            if (inputMaterial) inputMaterial.value = producto.material || '';
            const inputColorAcc = document.getElementById('formColorAccesorio');
            if (inputColorAcc) inputColorAcc.value = producto.color || '';
            const inputMarcas = document.getElementById('formMarcasCompatibles');
            if (inputMarcas) {
                inputMarcas.value = Array.isArray(producto.marcasCompatibles) 
                    ? producto.marcasCompatibles.join(', ') 
                    : (producto.marcasCompatibles || '');
            }
            const checkOriginal = document.getElementById('formEsOriginal');
            if (checkOriginal) checkOriginal.checked = producto.esOriginal !== false;
        }
    }

    actualizarCamposTipoForm();
    toggleProductFormModal(true);
}

function prepararEditarProducto(id, tipo) {
    let producto = null;
    if (tipo === 'celular') {
        producto = celularesGlobal.find(p => (p.idProducto || p.id) === id);
    } else {
        producto = accesoriosGlobal.find(p => (p.idProducto || p.id) === id);
    }

    if (producto) {
        abrirModalFormProducto('editar', producto);
    } else {
        alert('No se encontró la información del producto.');
    }
}

async function guardarProductoAdmin(event) {
    event.preventDefault();
    const token = localStorage.getItem('sintaxis_token');
    const errorMsg = document.getElementById('productFormErrorMsg');
    errorMsg.style.display = 'none';

    if (!token) {
        alert('Debe iniciar sesión como ADMIN o EMPLEADO para realizar esta operación.');
        return;
    }

    const id = document.getElementById('formProductId').value;
    const esEdicion = !!id;
    const tipo = document.getElementById('formTipoProducto').value;

    const nombre = document.getElementById('formNombre').value.trim();
    const marca = document.getElementById('formMarca').value.trim();
    const descripcion = document.getElementById('formDescripcion').value.trim();
    const precio = parseFloat(document.getElementById('formPrecio').value);
    const stock = parseInt(document.getElementById('formStock').value, 10);
    const stockMinimo = parseInt(document.getElementById('formStockMinimo').value, 10);
    const imagenUrl = document.getElementById('formImagenUrl').value.trim();

    let dto = { nombre, marca, descripcion, precio, stock, stockMinimo, imagenUrl };
    let endpoint = '';

    if (tipo === 'celular') {
        endpoint = esEdicion ? `/api/celulares/${id}` : '/api/celulares';
        dto.modelo = document.getElementById('formModelo').value.trim();
        dto.sistemaOperativo = document.getElementById('formSistemaOperativo').value.trim();
        dto.almacenamientoGB = parseInt(document.getElementById('formAlmacenamientoGB').value, 10);
        dto.ramGB = parseInt(document.getElementById('formRamGB').value, 10);
        dto.procesador = document.getElementById('formProcesador').value.trim();
        dto.pantallaPulgadas = parseFloat(document.getElementById('formPantallaPulgadas').value);
        dto.bateriaMAh = parseInt(document.getElementById('formBateriaMAh').value, 10);
        dto.color = document.getElementById('formColorCelular').value.trim();
        dto.esLibre = document.getElementById('formEsLibre').checked;
    } else {
        endpoint = esEdicion ? `/api/accesorios/${id}` : '/api/accesorios';
        const subcatVal = document.getElementById('formSubcategoriaAccesorio')?.value;
        dto.subcategoriaId = subcatVal ? parseInt(subcatVal, 10) : null;
        dto.material = document.getElementById('formMaterial')?.value?.trim() || '';
        dto.color = document.getElementById('formColorAccesorio')?.value?.trim() || '';
        dto.esOriginal = document.getElementById('formEsOriginal')?.checked ?? true;
        
        const rawMarcas = document.getElementById('formMarcasCompatibles')?.value?.trim() || '';
        dto.marcasCompatibles = rawMarcas ? rawMarcas.split(',').map(m => m.trim()).filter(m => m.length > 0) : [];
    }

    try {
        const method = esEdicion ? 'PUT' : 'POST';
        const res = await fetch(endpoint, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || `Error al ${esEdicion ? 'actualizar' : 'crear'} el producto. Status: ${res.status}`);
        }

        alert(`¡Producto ${esEdicion ? 'actualizado' : 'creado'} con éxito!`);
        toggleProductFormModal(false);
        await cargarProductos();
        cargarProductosAdmin();
    } catch (err) {
        console.error(err);
        errorMsg.textContent = err.message || 'Ocurrió un error al procesar la solicitud.';
        errorMsg.style.display = 'block';
    }
}

async function eliminarProductoAdmin(id, tipo, nombre) {
    const token = localStorage.getItem('sintaxis_token');
    if (!token) {
        alert('Debe iniciar sesión para realizar esta operación.');
        return;
    }

    if (!confirm(`¿Está seguro de que desea eliminar el producto "${nombre}" (ID: ${id})?\nEsta acción realizará un borrado lógico.`)) {
        return;
    }

    const endpoint = tipo === 'celular' ? `/api/celulares/${id}` : `/api/accesorios/${id}`;

    try {
        const res = await fetch(endpoint, {
            method: 'DELETE',
            headers: {
                'Authorization': 'Bearer ' + token
            }
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || `No se pudo eliminar el producto. Status: ${res.status}`);
        }

        alert(`Producto "${nombre}" eliminado con éxito.`);
        await cargarProductos();
        cargarProductosAdmin();
    } catch (err) {
        console.error(err);
        alert(err.message || 'Error al eliminar el producto.');
    }
}

// --- Funciones para Gestión Dinámica de Subcategorías ---
let subcategoriasGlobal = [];

async function cargarSubcategorias() {
    try {
        const res = await fetch('/api/subcategorias');
        if (res.ok) {
            const data = await res.json();
            subcategoriasGlobal = Array.isArray(data) ? data : (data.content || []);
        } else {
            subcategoriasGlobal = [];
        }
    } catch (err) {
        console.error('Error al cargar subcategorías', err);
        subcategoriasGlobal = [];
    }
    poblarSelectSubcategoriasForm();
    renderizarFiltrosSubcategorias();
}

function poblarSelectSubcategoriasForm() {
    const select = document.getElementById('formSubcategoriaAccesorio');
    if (!select) return;

    if (subcategoriasGlobal.length === 0) {
        select.innerHTML = `<option value="">(Sin subcategorías registradas)</option>`;
        return;
    }

    select.innerHTML = subcategoriasGlobal.map(s => {
        const idSub = s.idSubcategoria || s.id || '';
        const nombreSub = typeof s === 'string' ? s : (s.nombre || '');
        return `<option value="${idSub}">${escapeHtml(nombreSub)}</option>`;
    }).join('');
}

function renderizarFiltrosSubcategorias() {
    const container = document.getElementById('subcategoriaContainer');
    if (!container) return;

    let html = `<span style="font-size: 0.75rem; font-weight: 600; text-transform: uppercase; color: var(--text-secondary); margin-right: 0.5rem; letter-spacing: 0.05em;">Subcategorías Accesorios:</span>`;

    const botonSubcategoria = (nombreSub) => {
        const isActive = subcategoriaActiva === nombreSub;
        return `<button type="button" class="btn subcat-btn ${isActive ? 'active' : ''}" data-subcategoria="${escapeHtml(nombreSub)}" style="padding: 0.35rem 0.75rem; font-size: 0.75rem; background: ${isActive ? 'var(--accent-cyan)' : 'transparent'}; color: ${isActive ? '#090A0F' : 'var(--text-secondary)'}; border: ${isActive ? 'none' : '1px solid var(--border-color)'}; font-weight: ${isActive ? '600' : '400'};">${escapeHtml(nombreSub)}</button>`;
    };

    html += botonSubcategoria('Todas');

    subcategoriasGlobal.forEach(s => {
        const nombreSub = typeof s === 'string' ? s : (s.nombre || '');
        if (!nombreSub || nombreSub.toLowerCase() === 'celulares' || nombreSub.toLowerCase() === 'celular') return;
        html += botonSubcategoria(nombreSub);
    });

    container.innerHTML = html;

    // Delegación de eventos: el nombre de la subcategoría NUNCA se interpreta como código JS
    // (evita XSS almacenado a través de nombres manipulados).
    if (!container.dataset.listenerAttached) {
        container.addEventListener('click', (event) => {
            const btn = event.target.closest('.subcat-btn');
            if (!btn) return;
            const nombre = btn.dataset.subcategoria;
            if (typeof nombre === 'string') cambiarSubcategoria(nombre);
        });
        container.dataset.listenerAttached = 'true';
    }
}

// --- Panel de Administración: Editar / Eliminar Subcategorías ---

function contarAccesoriosDeSubcategoria(idSubcategoria) {
    const idNum = Number(idSubcategoria);
    return (accesoriosGlobal || []).filter(a => {
        const idAcc = a.subcategoriaId != null
            ? Number(a.subcategoriaId)
            : (a.subcategoria ? Number(a.subcategoria.idSubcategoria) : NaN);
        return idAcc === idNum;
    }).length;
}

async function cargarSubcategoriasAdmin() {
    const tbody = document.getElementById('adminSubcategoriaTableBody');
    if (!tbody) return;

    tbody.innerHTML = `<tr><td colspan="4" class="loading">Cargando subcategorías...</td></tr>`;

    try {
        // Endpoint público: no se envía el token para que el listado siga funcionando
        // aunque la sesión haya expirado (las escrituras sí lo requieren).
        const res = await fetch('/api/subcategorias');

        if (!res.ok) throw new Error('No se pudieron cargar las subcategorías.');

        const data = await res.json();
        subcategoriasGlobal = Array.isArray(data) ? data : (data.content || []);
        renderizarTablaSubcategoriasAdmin();
        poblarSelectSubcategoriasForm();
        renderizarFiltrosSubcategorias();
    } catch (err) {
        console.error(err);
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state" style="padding: 2rem 1rem; color: var(--danger-rose);">
            <strong>Error al cargar subcategorías</strong><br><span style="font-size: 0.8rem;">${escapeHtml(err.message || '')}</span>
        </td></tr>`;
    }
}

function renderizarTablaSubcategoriasAdmin() {
    const tbody = document.getElementById('adminSubcategoriaTableBody');
    if (!tbody) return;

    if (!subcategoriasGlobal || subcategoriasGlobal.length === 0) {
        tbody.innerHTML = `<tr><td colspan="4" class="empty-state" style="padding: 3rem 1rem; text-align: center;">
            <strong>No hay subcategorías registradas.</strong>
            <p style="font-size: 0.8rem; color: var(--text-secondary); margin-top: 0.35rem;">Creá la primera con el botón “+ Nueva Subcategoría”.</p>
        </td></tr>`;
        return;
    }

    const filas = subcategoriasGlobal.map(s => {
        const id = Number(s.idSubcategoria || s.id);
        if (!Number.isInteger(id)) return '';
        const nombre = s.nombre || '';
        const cantidad = contarAccesoriosDeSubcategoria(id);
        const badgeColor = cantidad > 0 ? 'rgba(56, 189, 248, 0.15)' : 'rgba(148, 163, 184, 0.12)';
        const badgeText = cantidad > 0 ? 'var(--accent-cyan)' : 'var(--text-secondary)';

        return `<tr>
            <td style="padding: 0.8rem 1rem; font-family: 'JetBrains Mono', monospace; color: var(--text-secondary);">#${id}</td>
            <td style="padding: 0.8rem 1rem; font-weight: 500; color: var(--text-main);">${escapeHtml(nombre)}</td>
            <td style="padding: 0.8rem 1rem;">
                <span style="display: inline-flex; align-items: center; gap: 0.35rem; font-size: 0.75rem; font-weight: 600; padding: 0.2rem 0.55rem; border-radius: 999px; background: ${badgeColor}; color: ${badgeText};">${cantidad}</span>
            </td>
            <td style="padding: 0.8rem 1rem; text-align: right; white-space: nowrap;">
                <button class="btn" style="padding: 0.3rem 0.6rem; font-size: 0.75rem; background: rgba(56, 189, 248, 0.12); color: var(--accent-cyan); border: 1px solid rgba(56, 189, 248, 0.3); margin-right: 0.35rem;" onclick="abrirModalEditarSubcategoria(${id})">✏️ Editar</button>
                <button class="btn" style="padding: 0.3rem 0.6rem; font-size: 0.75rem; background: rgba(244, 63, 94, 0.12); color: var(--danger-rose); border: 1px solid rgba(244, 63, 94, 0.3);" onclick="eliminarSubcategoriaAdmin(${id})">🗑️ Eliminar</button>
            </td>
        </tr>`;
    }).join('');

    tbody.innerHTML = filas;
}

function toggleSubcategoriaModal(abrir) {
    const modal = document.getElementById('subcategoriaModal');
    const errorMsg = document.getElementById('subcatFormErrorMsg');
    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (modal) modal.style.display = abrir ? 'flex' : 'none';
}

function abrirModalNuevaSubcategoria() {
    const form = document.getElementById('subcategoriaForm');
    if (form) form.reset();

    const idInput = document.getElementById('subcatId');
    if (idInput) idInput.value = '';

    const title = document.getElementById('subcatModalTitle');
    if (title) title.innerHTML = '📂 Nueva Subcategoría';

    const hint = document.getElementById('subcatModalHint');
    if (hint) {
        hint.style.display = 'none';
        hint.textContent = '';
    }

    const submitBtn = document.getElementById('subcatSubmitBtn');
    if (submitBtn) submitBtn.textContent = 'Crear Subcategoría';

    toggleSubcategoriaModal(true);
    setTimeout(() => document.getElementById('subcatNombreInput')?.focus(), 50);
}

function abrirModalEditarSubcategoria(id) {
    const sub = (subcategoriasGlobal || []).find(s => Number(s.idSubcategoria || s.id) === Number(id));
    if (!sub) {
        mostrarToast('No se encontró la subcategoría seleccionada.', 'error');
        return;
    }

    const form = document.getElementById('subcategoriaForm');
    if (form) form.reset();

    document.getElementById('subcatId').value = sub.idSubcategoria || sub.id;
    document.getElementById('subcatNombreInput').value = sub.nombre || '';

    const title = document.getElementById('subcatModalTitle');
    if (title) title.innerHTML = '✏️ Editar Subcategoría';

    const cantidad = contarAccesoriosDeSubcategoria(sub.idSubcategoria || sub.id);
    const hint = document.getElementById('subcatModalHint');
    if (hint) {
        hint.textContent = cantidad > 0
            ? `Esta subcategoría tiene ${cantidad} accesorio(s) asociado(s). Al renombrarla se actualizará la etiqueta de esos productos.`
            : 'Esta subcategoría todavía no tiene accesorios asociados.';
        hint.style.display = 'block';
    }

    const submitBtn = document.getElementById('subcatSubmitBtn');
    if (submitBtn) submitBtn.textContent = 'Guardar Cambios';

    toggleSubcategoriaModal(true);
    setTimeout(() => document.getElementById('subcatNombreInput')?.focus(), 50);
}

async function guardarSubcategoriaAdmin(event) {
    event.preventDefault();
    const token = localStorage.getItem('sintaxis_token');
    const errorMsg = document.getElementById('subcatFormErrorMsg');
    const submitBtn = document.getElementById('subcatSubmitBtn');
    if (errorMsg) errorMsg.style.display = 'none';

    if (!token) {
        mostrarToast('Debe iniciar sesión como ADMIN o EMPLEADO.', 'error');
        return;
    }

    const id = document.getElementById('subcatId')?.value;
    const nombre = (document.getElementById('subcatNombreInput')?.value || '').trim();
    if (!nombre) {
        if (errorMsg) {
            errorMsg.textContent = 'El nombre de la subcategoría es obligatorio.';
            errorMsg.style.display = 'block';
        }
        return;
    }

    const esEdicion = !!id;
    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Guardando...';
    }

    try {
        const res = await fetch(esEdicion ? `/api/subcategorias/${id}` : '/api/subcategorias', {
            method: esEdicion ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ nombre })
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.error || errJson.message || 'No se pudo guardar la subcategoría.');
        }

        toggleSubcategoriaModal(false);
        mostrarToast(esEdicion ? `Subcategoría actualizada a "${escapeHtml(nombre)}".` : `Subcategoría "${escapeHtml(nombre)}" creada con éxito.`);

        await cargarSubcategorias();
        await cargarProductos();
        renderizarTablaSubcategoriasAdmin();
    } catch (err) {
        console.error(err);
        const mensaje = err.message || 'Error al guardar la subcategoría.';
        if (errorMsg) {
            errorMsg.textContent = mensaje;
            errorMsg.style.display = 'block';
        } else {
            mostrarToast(escapeHtml(mensaje), 'error');
        }
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = esEdicion ? 'Guardar Cambios' : 'Crear Subcategoría';
        }
    }
}

let subcategoriaPendienteEliminar = null;

function toggleEliminarSubcategoriaModal(abrir) {
    const modal = document.getElementById('subcategoriaDeleteModal');
    const errorMsg = document.getElementById('subcatDeleteErrorMsg');
    const confirmBtn = document.getElementById('subcatDeleteConfirmBtn');

    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (confirmBtn) {
        confirmBtn.disabled = false;
        confirmBtn.textContent = 'Eliminar definitivamente';
    }
    if (modal) modal.style.display = abrir ? 'flex' : 'none';

    if (!abrir) subcategoriaPendienteEliminar = null;
}

function eliminarSubcategoriaAdmin(id) {
    const token = localStorage.getItem('sintaxis_token');
    if (!token) {
        mostrarToast('Debe iniciar sesión como ADMIN o EMPLEADO.', 'error');
        return;
    }

    const idNum = Number(id);
    if (!Number.isInteger(idNum)) {
        mostrarToast('Subcategoría inválida.', 'error');
        return;
    }

    const sub = (subcategoriasGlobal || []).find(s => Number(s.idSubcategoria || s.id) === idNum);
    const nombre = sub ? sub.nombre : `#${idNum}`;
    const cantidad = contarAccesoriosDeSubcategoria(idNum);

    subcategoriaPendienteEliminar = { id: idNum, nombre, cantidad };

    document.getElementById('subcatDeleteNombre').textContent = `"${nombre}"`;

    const warning = document.getElementById('subcatDeleteWarning');
    if (warning) {
        if (cantidad > 0) {
            warning.textContent = `⚠️ ${cantidad} accesorio(s) asociado(s) quedarán sin subcategoría asignada. No se eliminarán del inventario ni del historial de ventas.`;
            warning.style.display = 'block';
        } else {
            warning.style.display = 'none';
            warning.textContent = '';
        }
    }

    toggleEliminarSubcategoriaModal(true);
}

async function confirmarEliminacionSubcategoria() {
    const token = localStorage.getItem('sintaxis_token');
    const pendiente = subcategoriaPendienteEliminar;
    const errorMsg = document.getElementById('subcatDeleteErrorMsg');
    const confirmBtn = document.getElementById('subcatDeleteConfirmBtn');

    if (!token) {
        mostrarToast('Debe iniciar sesión como ADMIN o EMPLEADO.', 'error');
        return;
    }
    if (!pendiente) return;

    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.textContent = 'Eliminando...';
    }

    try {
        const res = await fetch(`/api/subcategorias/${pendiente.id}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.error || errJson.message || 'No se pudo eliminar la subcategoría.');
        }

        toggleEliminarSubcategoriaModal(false);

        // Se muestra el nombre como texto escapado para el toast (se inyecta con innerHTML).
        mostrarToast(`Subcategoría "${escapeHtml(pendiente.nombre)}" eliminada.`);

        if (subcategoriaActiva === pendiente.nombre) {
            subcategoriaActiva = 'Todas';
        }

        await cargarSubcategorias();
        await cargarProductos();
        renderizarTablaSubcategoriasAdmin();
    } catch (err) {
        console.error(err);
        const mensaje = err.message || 'Error al eliminar la subcategoría.';
        if (errorMsg) {
            errorMsg.textContent = mensaje;
            errorMsg.style.display = 'block';
        } else {
            mostrarToast(escapeHtml(mensaje), 'error');
        }
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.textContent = 'Eliminar definitivamente';
        }
    }
}

// --- Gestión de Stock Rápido en Panel Admin ---
function toggleStockModal(abrir) {
    const modal = document.getElementById('stockModal');
    const errorMsg = document.getElementById('stockFormErrorMsg');
    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

function abrirModalAgregarStock(id, tipo, nombre, stockActual) {
    document.getElementById('stockProdId').value = id;
    document.getElementById('stockProdTipo').value = tipo;
    document.getElementById('stockCantidadInput').value = 10;
    document.getElementById('stockProdInfo').innerHTML = `Producto: <strong>${nombre}</strong> (Tipo: <span style="color: var(--accent-cyan);">${tipo === 'celular' ? 'Celular' : 'Accesorio'}</span>)<br>Stock actual: <span style="font-family: 'JetBrains Mono', monospace; font-weight: 600; color: var(--accent-cyan);">${stockActual} unidades</span>`;
    toggleStockModal(true);
}

async function ejecutarAgregarStock(event) {
    event.preventDefault();
    const token = localStorage.getItem('sintaxis_token');
    const errorMsg = document.getElementById('stockFormErrorMsg');
    if (errorMsg) errorMsg.style.display = 'none';

    if (!token) {
        alert('Debe iniciar sesión como ADMIN o EMPLEADO.');
        return;
    }

    const id = document.getElementById('stockProdId').value;
    const tipo = document.getElementById('stockProdTipo').value;
    const cantidadAgregar = parseInt(document.getElementById('stockCantidadInput').value, 10);

    if (isNaN(cantidadAgregar) || cantidadAgregar <= 0) {
        if (errorMsg) {
            errorMsg.textContent = 'Por favor ingrese una cantidad válida mayor a 0.';
            errorMsg.style.display = 'block';
        }
        return;
    }

    // Buscar producto actual en memoria para obtener sus datos completos y sumar al stock
    let producto = null;
    if (tipo === 'celular') {
        producto = celularesGlobal.find(p => (p.idProducto || p.id) == id);
    } else {
        producto = accesoriosGlobal.find(p => (p.idProducto || p.id) == id);
    }

    if (!producto) {
        alert('No se encontró el producto en la memoria.');
        return;
    }

    const nuevoStock = (producto.stock || 0) + cantidadAgregar;
    const endpoint = tipo === 'celular' ? `/api/celulares/${id}` : `/api/accesorios/${id}`;

    // Construir DTO con todos los campos requeridos para la actualización PUT
    let dto = {
        nombre: producto.nombre,
        marca: producto.marca,
        descripcion: producto.descripcion,
        precio: producto.precio,
        stock: nuevoStock,
        stockMinimo: producto.stockMinimo !== undefined ? producto.stockMinimo : 5,
        imagenUrl: producto.imagenUrl
    };

    if (tipo === 'celular') {
        dto.modelo = producto.modelo;
        dto.sistemaOperativo = producto.sistemaOperativo;
        dto.almacenamientoGB = producto.almacenamientoGB;
        dto.ramGB = producto.ramGB;
        dto.procesador = producto.procesador;
        dto.pantallaPulgadas = producto.pantallaPulgadas;
        dto.bateriaMAh = producto.bateriaMAh;
        dto.color = producto.color;
        dto.esLibre = producto.esLibre;
    } else {
        dto.subcategoriaId = producto.subcategoriaId || (producto.subcategoria && producto.subcategoria.idSubcategoria);
        dto.material = producto.material;
        dto.color = producto.color;
        dto.esOriginal = producto.esOriginal;
        dto.marcasCompatibles = producto.marcasCompatibles || [];
    }

    try {
        const res = await fetch(endpoint, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify(dto)
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || `Error al actualizar el stock. Status: ${res.status}`);
        }

        mostrarToast(`¡Stock actualizado con éxito! Se agregaron ${cantidadAgregar} unidades. Stock total: ${nuevoStock}`, 'success');
        toggleStockModal(false);
        await cargarProductos();
        cargarProductosAdmin();
    } catch (err) {
        console.error(err);
        if (errorMsg) {
            errorMsg.textContent = err.message || 'Error al actualizar el stock.';
            errorMsg.style.display = 'block';
        } else {
            mostrarToast(err.message || 'Error al actualizar el stock.', 'error');
        }
    }
}

// --- Funcionalidad de Recuperación de Contraseña ---
function toggleForgotPasswordModal(abrir) {
    const modal = document.getElementById('forgotPasswordModal');
    if (!modal) return;
    modal.style.display = abrir ? 'flex' : 'none';
    
    // Si se abre, resetear formularios y mensajes
    if (abrir) {
        const forgotForm = document.getElementById('forgotForm');
        const resetForm = document.getElementById('resetForm');
        if (forgotForm) forgotForm.reset();
        if (resetForm) resetForm.reset();
        
        const forgotErr = document.getElementById('forgotErrorMsg');
        const forgotSucc = document.getElementById('forgotSuccessMsg');
        const resetErr = document.getElementById('resetErrorMsg');
        const resetSucc = document.getElementById('resetSuccessMsg');
        
        if (forgotErr) forgotErr.style.display = 'none';
        if (forgotSucc) forgotSucc.style.display = 'none';
        if (resetErr) resetErr.style.display = 'none';
        if (resetSucc) resetSucc.style.display = 'none';
        
        // Mostrar paso 1 por defecto
        if (forgotForm) forgotForm.style.display = 'flex';
        if (resetForm) resetForm.style.display = 'none';
    }
}

function abrirModalForgotPassword() {
    toggleLoginModal(false);
    toggleForgotPasswordModal(true);
}

function alternarModalResetPasswordDirecto() {
    const forgotForm = document.getElementById('forgotForm');
    const resetForm = document.getElementById('resetForm');
    if (!forgotForm || !resetForm) return;

    if (forgotForm.style.display === 'none') {
        forgotForm.style.display = 'flex';
        resetForm.style.display = 'none';
    } else {
        forgotForm.style.display = 'none';
        resetForm.style.display = 'flex';
    }
}

async function ejecutarSolicitudForgotPassword(event) {
    event.preventDefault();
    const email = document.getElementById('forgotEmail').value.trim();
    const errorMsg = document.getElementById('forgotErrorMsg');
    const successMsg = document.getElementById('forgotSuccessMsg');
    
    if (errorMsg) errorMsg.style.display = 'none';
    if (successMsg) successMsg.style.display = 'none';

    try {
        const res = await fetch('/api/auth/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email })
        });

        // El endpoint devuelve 204 No Content incluso si no existe por seguridad,
        // pero informamos al usuario exitosamente
        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || 'Error al solicitar la recuperación de contraseña.');
        }

        if (successMsg) {
            successMsg.textContent = '¡Código enviado! Revisa los logs de la consola para ver tu código de 6 dígitos.';
            successMsg.style.display = 'block';
        }

        // Cambiar automáticamente al formulario de reset para comodidad de prueba
        setTimeout(() => {
            const tokenInput = document.getElementById('resetTokenInput');
            if (tokenInput) tokenInput.focus();
            alternarModalResetPasswordDirecto();
        }, 2000);

    } catch (err) {
        console.error(err);
        if (errorMsg) {
            errorMsg.textContent = err.message || 'Error de conexión.';
            errorMsg.style.display = 'block';
        }
    }
}

async function ejecutarResetPassword(event) {
    event.preventDefault();
    const token = document.getElementById('resetTokenInput').value.trim();
    const passwordNueva = document.getElementById('resetPasswordNueva').value;
    const errorMsg = document.getElementById('resetErrorMsg');
    const successMsg = document.getElementById('resetSuccessMsg');

    if (errorMsg) errorMsg.style.display = 'none';
    if (successMsg) successMsg.style.display = 'none';

    try {
        const res = await fetch('/api/auth/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token, passwordNueva })
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || 'Token inválido o expirado.');
        }

        if (successMsg) {
            successMsg.textContent = '¡Contraseña restablecida con éxito! Ya puedes iniciar sesión.';
            successMsg.style.display = 'block';
        }

        mostrarToast('¡Contraseña actualizada con éxito!', 'success');

        setTimeout(() => {
            toggleForgotPasswordModal(false);
            toggleLoginModal(true);
        }, 1800);

    } catch (err) {
        console.error(err);
        if (errorMsg) {
            errorMsg.textContent = err.message || 'Error al restablecer la contraseña.';
            errorMsg.style.display = 'block';
        }
    }
}

actualizarContadorCarrito();
renderizarCarrito();

// Carga inicial
actualizarUIAuth();
cargarProductos();
cargarSubcategorias();

// ==================== GESTIÓN DE INACTIVIDAD (IDLE TIMEOUT - 5 MINUTOS) ====================
let inactivityTimer = null;
let countdownInterval = null;
let remainingSeconds = 30;
const IDLE_TIME_MS = 4 * 60 * 1000 + 30 * 1000; // 4 minutos 30 segundos antes de mostrar advertencia
const COUNTDOWN_TIME_S = 30; // 30 segundos de cuenta regresiva en el modal

function iniciarTemporizadorInactividad() {
    if (!localStorage.getItem('sintaxis_token')) {
        detenerTemporizadorInactividad();
        return;
    }
    reiniciarTemporizadorInactividad();

    const eventos = ['mousemove', 'keydown', 'click', 'scroll', 'touchstart', 'input'];
    eventos.forEach(evento => {
        window.removeEventListener(evento, registrarActividadUsuario);
        window.addEventListener(evento, registrarActividadUsuario, { passive: true });
    });
}

function registrarActividadUsuario() {
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

// Si ya hay una sesión activa al cargar la página, iniciamos el control de inactividad
if (localStorage.getItem('sintaxis_token')) {
    iniciarTemporizadorInactividad();
}
// --- Punto 2: Detalle Rápido / Vista Modal del Producto ---
function abrirModalDetalleProducto(p) {
    const modal = document.getElementById('productDetailModal');
    const content = document.getElementById('detailModalContent');
    const titleEl = document.getElementById('detailModalTitle');
    if (!modal || !content) return;

    titleEl.innerHTML = `🔍 Detalle Rápido: ${escapeHtml(p.nombre)}`;

    const stockActual = p.stock !== undefined ? p.stock : 0;
    const esAgotado = stockActual <= 0;
    const esStockBajo = !esAgotado && (stockActual <= p.stockMinimo);

    const imagenDefault = p.imagenUrl && p.imagenUrl.trim() !== '' 
        ? (p.imagenUrl.startsWith('http://localhost:8080/api/productos/') 
            ? p.imagenUrl.replace('/api/productos/', '/api/imagenes/') 
            : p.imagenUrl)
        : 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';

    let badgeHtml = '';
    if (p._tipo === 'celular') {
        badgeHtml = `<span class="badge" style="background: rgba(56, 189, 248, 0.15); color: var(--accent-cyan); border-color: rgba(56,189,248,0.3);">Celular</span>`;
    } else {
        const sub = p.subcategoriaNombre || p.subcategoria || 'Accesorio';
        badgeHtml = `<span class="badge" style="background: rgba(236, 72, 153, 0.15); color: #ec4899; border-color: rgba(236,72,153,0.3);">${escapeHtml(sub)}</span>`;
    }

    if (esAgotado) {
        badgeHtml += ` <span class="badge" style="background: rgba(244, 63, 94, 0.2); color: var(--danger-rose); border-color: rgba(244, 63, 94, 0.4); font-weight: 700;">AGOTADO</span>`;
    } else if (esStockBajo) {
        badgeHtml += ` <span class="badge badge-warning" style="margin-left: 0.25rem;">Stock Bajo (${stockActual} disp.)</span>`;
    } else {
        badgeHtml += ` <span class="badge" style="background: rgba(16, 185, 129, 0.15); color: var(--success-emerald); border-color: rgba(16,185,129,0.3); margin-left: 0.25rem;">Stock Disponible (${stockActual})</span>`;
    }

    // Construir tabla de especificaciones técnicas según sea celular o accesorio
    let specsHtml = '';
    if (p._tipo === 'celular') {
        specsHtml = `
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1rem; background: var(--bg-canvas); padding: 1rem; border-radius: var(--radius); border: 1px solid var(--border-color); font-size: 0.85rem;">
                <div>📱 <strong>Modelo:</strong> ${escapeHtml(p.modelo || 'N/D')}</div>
                <div>🏷️ <strong>Marca:</strong> ${escapeHtml(p.marca || 'N/D')}</div>
                <div>⚙️ <strong>OS:</strong> ${escapeHtml(p.sistemaOperativo || 'Android / iOS')}</div>
                <div>💾 <strong>Almacenamiento:</strong> ${p.almacenamientoGB ? p.almacenamientoGB + ' GB' : 'N/D'}</div>
                <div>⚡ <strong>Memoria RAM:</strong> ${p.ramGB ? p.ramGB + ' GB' : 'N/D'}</div>
                <div>🧠 <strong>Procesador:</strong> ${escapeHtml(p.procesador || 'N/D')}</div>
                <div>📺 <strong>Pantalla:</strong> ${p.pantallaPulgadas ? p.pantallaPulgadas + '"' : 'N/D'}</div>
                <div>🔋 <strong>Batería:</strong> ${p.bateriaMAh ? p.bateriaMAh + ' mAh' : 'N/D'}</div>
                <div>🎨 <strong>Color:</strong> ${escapeHtml(p.color || 'Estándar')}</div>
                <div>🔓 <strong>Libre:</strong> ${p.esLibre !== false ? 'Sí (Liberado)' : 'No'}</div>
            </div>
        `;
    } else {
        const sub = p.subcategoriaNombre || p.subcategoria || 'Accesorio';
        specsHtml = `
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem; margin-top: 1rem; background: var(--bg-canvas); padding: 1rem; border-radius: var(--radius); border: 1px solid var(--border-color); font-size: 0.85rem;">
                <div>📁 <strong>Subcategoría:</strong> ${escapeHtml(sub)}</div>
                <div>🏷️ <strong>Marca:</strong> ${escapeHtml(p.marca || 'Genérica')}</div>
                <div>🎨 <strong>Color:</strong> ${escapeHtml(p.color || 'Universal')}</div>
                <div>✨ <strong>Original:</strong> ${p.esOriginal !== false ? 'Sí (Original)' : 'Genérico'}</div>
                <div style="grid-column: 1 / -1;">🔗 <strong>Compatibilidad:</strong> ${escapeHtml(Array.isArray(p.marcasCompatibles) ? p.marcasCompatibles.join(', ') : (p.marcasCompatibles || 'Universal para Smartphones'))}</div>
            </div>
        `;
    }

    content.innerHTML = `
        <div style="display: flex; flex-direction: column; gap: 1rem; align-items: center;">
            <div style="width: 100%; max-height: 340px; border-radius: var(--radius); overflow: hidden; background: var(--bg-canvas); border: 1px solid var(--border-color); display: flex; align-items: center; justify-content: center; padding: 1rem;">
                <img src="${imagenDefault}" alt="${escapeHtml(p.nombre)}" style="max-height: 300px; max-width: 100%; object-fit: contain;" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80';">
            </div>
            <div style="font-size: 0.75rem; color: var(--text-secondary); text-align: center;">ID Producto: #${p.idProducto || p.id}</div>
        </div>
        <div style="display: flex; flex-direction: column; gap: 1rem;">
            <div>
                <div style="margin-bottom: 0.5rem;">${badgeHtml}</div>
                <h2 style="font-size: 1.35rem; font-weight: 700; color: var(--text-main); line-height: 1.3; margin-bottom: 0.5rem;">${escapeHtml(p.nombre)}</h2>
                <div style="font-size: 0.875rem; color: var(--text-secondary); line-height: 1.5;">${escapeHtml(p.descripcion || 'Sin descripción detallada disponible.')}</div>
            </div>

            ${specsHtml}

            <div style="display: flex; align-items: center; justify-content: space-between; margin-top: 0.5rem; background: #0E1017; padding: 0.5rem 0.75rem; border-radius: var(--radius); border: 1px solid var(--border-color);">
                <div style="min-width: 0; margin-right: 0.5rem;">
                    <div style="font-size: 0.55rem; text-transform: uppercase; color: var(--text-secondary); font-weight: 600;">Precio Final</div>
                    <div style="font-size: 1rem; font-weight: 700; font-family: 'JetBrains Mono', monospace; color: var(--accent-cyan);">$${Number(p.precio || 0).toLocaleString('es-ES', { minimumFractionDigits: 2 })}</div>
                </div>
                <div style="display: flex; align-items: center; justify-content: center; flex-shrink: 0;">
                    ${esAgotado 
                        ? `<button class="btn btn-secondary" style="padding: 0.3rem 0.5rem; font-size: 0.65rem; opacity: 0.5; cursor: not-allowed; white-space: nowrap; line-height: 1.1;" disabled>🚫 Agotado</button>`
                        : `<button class="btn btn-primary" style="padding: 0.3rem 0.5rem; font-size: 0.65rem; white-space: nowrap; line-height: 1.1;" onclick='agregarAlCarritoDesdeDetalle(${JSON.stringify(p).replace(/'/g, "&#39;")})'>🛒 Agregar al Carrito</button>`
                    }
                </div>
            </div>
        </div>
    `;

    modal.style.display = 'flex';
}

function cerrarModalDetalleProducto() {
    const modal = document.getElementById('productDetailModal');
    if (modal) modal.style.display = 'none';
}

function agregarAlCarritoDesdeDetalle(p) {
    agregarAlCarrito(p);
    cerrarModalDetalleProducto();
}

// Cerrar modal al presionar ESC o hacer clic fuera del contenido
window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        cerrarModalDetalleProducto();
    }
});
