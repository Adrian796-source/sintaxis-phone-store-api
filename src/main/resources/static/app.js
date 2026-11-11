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
                const sub = p.subcategoria ? p.subcategoria.trim().toLowerCase() : '';
                const buscada = subcategoriaActiva.trim().toLowerCase();
                return sub === buscada || (sub && sub.includes(buscada));
            });
        }
    }

    const query = document.getElementById('searchInput').value.trim().toLowerCase();
    const estadoFiltro = document.getElementById('filterEstado').value;

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
            const sub = acc.subcategoriaNombre || acc.subcategoria || 'General';
            if (!grupos[sub]) grupos[sub] = [];
            grupos[sub].push(acc);
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
    const esStockBajo = p.stock <= p.stockMinimo;
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

    if (esStockBajo) {
        badgeHtml += ` <span class="badge badge-warning" style="margin-left: 0.25rem;">Stock Bajo</span>`;
    }

    return `
        <div class="product-card">
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
                    <button class="btn btn-primary" style="padding: 0.5rem 1rem; font-size: 0.75rem;" onclick='agregarAlCarrito(${JSON.stringify(p).replace(/'/g, "&#39;")})'>
                        🛒 Agregar
                    </button>
                </div>
            </div>
        </div>
    `;
}

function actualizarMetricas(productos) {
    const total = productos.length;
    const activos = productos.filter(p => p.activo).length;
    const stockBajo = productos.filter(p => p.stock <= p.stockMinimo).length;

    document.getElementById('metric-total').textContent = total;
    document.getElementById('metric-activos').textContent = activos;
    document.getElementById('metric-stock-bajo').textContent = stockBajo;
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
    
    if (existente) {
        if (existente.cantidad < producto.stock) {
            existente.cantidad++;
        } else {
            alert('No hay suficiente stock disponible.');
            return;
        }
    } else {
        if (producto.stock > 0) {
            carrito.push({ ...producto, cantidad: 1 });
        } else {
            alert('Producto sin stock.');
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
        carrito[index].cantidad += delta;
        if (carrito[index].cantidad <= 0) {
            carrito.splice(index, 1);
        } else {
            const item = carrito[index];
            if (item.cantidad > item.stock) {
                item.cantidad = item.stock;
                alert('Ha alcanzado el límite de stock disponible.');
            }
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
    alert('¡Compra simulada con éxito! Gracias por su pedido en Sintaxis Kyū.');
    carrito = [];
    guardarCarrito();
    toggleCartDrawer();
}

// --- Autenticación y Control de Sesión ---
function actualizarUIAuth() {
    const container = document.getElementById('auth-action-container');
    const syncContainer = document.getElementById('sync-action-container');
    const token = localStorage.getItem('sintaxis_token');
    const rol = localStorage.getItem('sintaxis_rol');

    if (syncContainer) {
        if (token && (rol === 'ADMIN' || rol === 'EMPLEADO')) {
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
        `;
        if (rol === 'ADMIN' || rol === 'EMPLEADO') {
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
        
        localStorage.setItem('sintaxis_token', data.token);
        localStorage.setItem('sintaxis_rol', data.rol);
        localStorage.setItem('sintaxis_email', data.email);

        toggleLoginModal(false);
        actualizarUIAuth();
        alert(`¡Bienvenido de nuevo, ${data.email} (${data.rol})!`);
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
        actualizarUIAuth();
        alert('Sesión cerrada correctamente.');
    }
}

function irPanelAdmin() {
    toggleAdminModal(true);
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
        localStorage.setItem('sintaxis_token', data.token);
        localStorage.setItem('sintaxis_rol', data.rol);
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
    const form = document.getElementById('userForm');
    if (form) form.reset();
    toggleUserFormModal(true);
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
        if (u.rol === 'ADMIN') badgeRol = 'badge-warning';
        else if (u.rol === 'EMPLEADO') badgeRol = 'badge-active';

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
                <td style="padding: 0.8rem 1rem; text-align: right;">
                    <span style="font-size: 0.75rem; color: var(--text-muted);">Sistema</span>
                </td>
            </tr>
        `;
    }).join('');
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

    const dto = { nombre, apellido, email, password, rol };

    try {
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
        toggleUserFormModal(false);
        cargarUsuariosAdmin();
    } catch (err) {
        console.error(err);
        errorMsg.textContent = err.message || 'Error al crear usuario.';
        errorMsg.style.display = 'block';
    }
}

// --- Panel Admin & CRUD con JWT ---
let adminTabActiva = 'todos';

function toggleAdminModal(abrir) {
    const token = localStorage.getItem('sintaxis_token');
    const rol = localStorage.getItem('sintaxis_rol');

    if (abrir) {
        if (!token || (rol !== 'ADMIN' && rol !== 'EMPLEADO')) {
            alert('Acceso denegado: No tienes permisos de Administrador o Empleado.');
            return;
        }
    }

    const modal = document.getElementById('adminModal');
    if (abrir) {
        modal.style.display = 'flex';
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

    ['todos', 'celulares', 'accesorios', 'usuarios', 'clientes', 'ventas'].forEach(t => {
        const btn = document.getElementById(`admin-tab-${t}`);
        if (btn) {
            if (t === tab) {
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
        }
    });

    const headerActionsContainer = document.querySelector('#adminModal div > div > div:last-child');
    const existingBtn = document.getElementById('btnAdminAccionPrincipal');
    if (existingBtn) existingBtn.remove();

    if (tab === 'usuarios') {
        if (prodSection) prodSection.style.display = 'none';
        if (userSection) userSection.style.display = 'block';
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
        if (ventasSection) ventasSection.style.display = 'none';
        if (clientesSection) clientesSection.style.display = 'block';
        if (searchInput) searchInput.style.display = 'none';

        cargarClientesAdmin();
    } else if (tab === 'ventas') {
        if (prodSection) prodSection.style.display = 'none';
        if (userSection) userSection.style.display = 'none';
        if (ventasSection) ventasSection.style.display = 'flex';
        if (clientesSection) clientesSection.style.display = 'none';
        if (searchInput) searchInput.style.display = 'none';

        cargarVentasAdmin();
    } else {
        if (prodSection) prodSection.style.display = 'block';
        if (userSection) userSection.style.display = 'none';
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
        tbody.innerHTML = `<tr><td colspan="5" class="empty-state" style="padding: 3rem 1rem;">No hay clientes registrados en el sistema.</td></tr>`;
        return;
    }

    tbody.innerHTML = clientes.map(c => {
        const nombreCompleto = `${c.nombre || ''} ${c.apellido || ''}`.trim() || 'Sin Nombre';
        const email = c.email || 'Sin email';
        const telefono = c.telefono || 'Sin tel.';
        const direccion = c.direccion || 'Sin dir.';

        return `
            <tr>
                <td style="padding: 0.8rem 1rem;">
                    <div class="product-title" style="font-size: 0.85rem;">${escapeHtml(nombreCompleto)}</div>
                    <div style="font-size: 0.70rem; color: var(--text-muted); font-family: 'JetBrains Mono', monospace;">ID: #${c.idCliente || c.id}</div>
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
                <td style="padding: 0.8rem 1rem; text-align: right;">
                    <span class="badge badge-active">Activo</span>
                </td>
            </tr>
        `;
    }).join('');
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
        const res = await fetch('/api/ventas', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (res.status === 401 || res.status === 403) {
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
                    #${v.idVenta || v.id}
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
                    <select onchange="cambiarEstadoVentaAdmin(${v.idVenta || v.id}, this.value)" style="background: var(--bg-canvas); border: 1px solid var(--border-color); color: var(--text-main); padding: 0.3rem 0.5rem; border-radius: var(--radius); font-size: 0.75rem;">
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
    
    const isTodasActive = subcategoriaActiva === 'Todas';
    html += `<button class="btn subcat-btn ${isTodasActive ? 'active' : ''}" style="padding: 0.35rem 0.75rem; font-size: 0.75rem; background: ${isTodasActive ? 'var(--accent-cyan)' : 'transparent'}; color: ${isTodasActive ? '#090A0F' : 'var(--text-secondary)'}; border: ${isTodasActive ? 'none' : '1px solid var(--border-color)'}; font-weight: ${isTodasActive ? '600' : '400'};" onclick="cambiarSubcategoria('Todas')">Todas</button>`;

    subcategoriasGlobal.forEach(s => {
        const nombreSub = typeof s === 'string' ? s : (s.nombre || '');
        if (!nombreSub) return;
        const isActive = subcategoriaActiva === nombreSub;
        html += `<button class="btn subcat-btn ${isActive ? 'active' : ''}" style="padding: 0.35rem 0.75rem; font-size: 0.75rem; background: ${isActive ? 'var(--accent-cyan)' : 'transparent'}; color: ${isActive ? '#090A0F' : 'var(--text-secondary)'}; border: ${isActive ? 'none' : '1px solid var(--border-color)'}; font-weight: ${isActive ? '600' : '400'};" onclick="cambiarSubcategoria('${escapeHtml(nombreSub)}')">${escapeHtml(nombreSub)}</button>`;
    });

    container.innerHTML = html;
}

function toggleSubcatFormModal(abrir) {
    const modal = document.getElementById('subcatFormModal');
    const errorMsg = document.getElementById('subcatFormErrorMsg');
    if (errorMsg) {
        errorMsg.style.display = 'none';
        errorMsg.textContent = '';
    }
    if (modal) {
        modal.style.display = abrir ? 'flex' : 'none';
    }
}

function abrirModalNuevaSubcategoria() {
    const form = document.getElementById('subcatForm');
    if (form) form.reset();
    toggleSubcatFormModal(true);
}

async function guardarSubcategoriaAdmin(event) {
    event.preventDefault();
    const token = localStorage.getItem('sintaxis_token');
    const errorMsg = document.getElementById('subcatFormErrorMsg');
    if (errorMsg) errorMsg.style.display = 'none';

    if (!token) {
        alert('Debe iniciar sesión como ADMIN o EMPLEADO.');
        return;
    }

    const nombre = document.getElementById('formSubcatNombre').value.trim();
    if (!nombre) return;

    try {
        const res = await fetch('/api/subcategorias', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ nombre })
        });

        if (!res.ok) {
            const errJson = await res.json().catch(() => ({}));
            throw new Error(errJson.message || 'Error al crear la subcategoría.');
        }

        alert(`¡Subcategoría "${nombre}" creada con éxito!`);
        toggleSubcatFormModal(false);
        await cargarSubcategorias();
    } catch (err) {
        console.error(err);
        if (errorMsg) {
            errorMsg.textContent = err.message || 'Error al guardar subcategoría.';
            errorMsg.style.display = 'block';
        } else {
            alert(err.message || 'Error al guardar subcategoría.');
        }
    }
}

// Inicializar contador y renderizado de carrito al cargar
actualizarContadorCarrito();
renderizarCarrito();

// Carga inicial
actualizarUIAuth();
cargarProductos();
cargarSubcategorias();
