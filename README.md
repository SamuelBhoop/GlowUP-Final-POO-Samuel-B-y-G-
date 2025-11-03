
<p align="center">
  <img width="1200" height="630" alt="glowup-social-dark-onyx" src="https://github.com/user-attachments/assets/d1ea8a59-a3e3-43f2-a492-2d7f4988372f" />

</p>

<h1 align="center"> GlowUp — POO en Java (Consola)</h1>

<p align="center">
  App de consola para una tienda de cosméticos: catálogo, carrito y compras.<br/>
  Demuestra <b>abstracción, herencia, polimorfismo, encapsulamiento, interfaces</b>, lambdas y colecciones.
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17+-red?logo=java" />
  <img alt="Status" src="https://img.shields.io/badge/status-demo-green" />
  <img alt="Type" src="https://img.shields.io/badge/type-académico-blue" />
</p>

---
## 👥 Usuarios de prueba
- Dueña: `sakura@glowup.com` / `1234`
- AdminContenido: `editor@glowup.com` / `1234`
- Crea un Cliente desde el menú para comprar.

## Funcionalidades

- 👤 **Usuarios**: registro y log-in (Cliente).  
- 🧾 **Productos**: alta (AdminContenido/Dueña) y listado.  
- 🛒 **Carrito**: agregar ítems y **checkout** (genera compra y descuenta stock).  
- 🔐 **Roles**: `Cliente`, `AdministradorContenido`, `AdministradorUsuario`, `Duena`, `DesarrolladorProducto`.  
- 🕴️ **Operaciones** (submenú solo para `Duena`):  
  - `Fabrica` (planta física)  
  - `TrabajadorEsclavizado` (asignación a fábrica)  
  - `RegistroEsclavos` (control exclusivo de `Duena`)  
  - `ConsejoSombrio` (miembros: AdminContenido/AdminUsuario)

---
## 🧱 Paquetes

| Paquete     | Clases principales |
|-------------|--------------------|
| **app**     | Main |
| **dominio** | Usuario (abstracta), Cliente, AdministradorContenido, AdministradorUsuario, Duena, DesarrolladorProducto, Rol |
| **comercio**| Producto, Categoria, Carrito, LineaCarrito, Compra, LineaCompra, EstadoCompra |
| **pago**    | MetodoPago, TipoMetodoPago |
| **servicios** | AuthService, ProductService, CartService, PurchaseService, FabricaService, TrabajadorService, ConsejoSombrioService, RegistroEsclavosService |
| **produccion** | Fabrica |
| **operaciones** | TrabajadorEsclavizado, RegistroEsclavos, ConsejoSombrio |
<img width="1024" height="1024" alt="2b5ef72e-c854-476a-8dee-dc2a62959c5d" src="https://github.com/user-attachments/assets/b47b6fa5-f09d-4437-bf44-5ec940388b80" />

<h2 id="persistencia">💾 Persistencia (MySQL + JDBC)</h2> <p> El proyecto integra <b>JDBC puro</b> con <b>MySQL</b>. La conexión sale de <code>config.Database.getConnection()</code> (credenciales en <code>db.properties</code> en el classpath).<br/> La lógica de acceso a datos vive en clases <b>DAO</b> dentro de <code>servicios/</code>. </p> <p align="center"> <img alt="DB" src="https://img.shields.io/badge/MySQL-8.x-blue?logo=mysql" /> <img alt="JDBC" src="https://img.shields.io/badge/JDBC-Driver%20MySQL-informational" /> <img alt="Scope" src="https://img.shields.io/badge/persistencia-usuarios%2C%20categor%C3%ADas%2C%20productos%2C%20f%C3%A1brica%2Ftrabajador%2C%20consejos%2C%20carrito-success" /> </p>
🔗 Configuración

src/main/resources/db.properties

url=jdbc:mysql://localhost:3306/tienda?useSSL=false&serverTimezone=UTC

🧠 Clases con persistencia 
| Dominio / Módulo              | Tablas                                                | DAO principal              | Operaciones clave                                                                                      |
| ----------------------------- | ----------------------------------------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------ |
| **Usuarios (login/roles)**    | `usuario`                                             | `UsuarioDAO`               | Insertar cliente (guarda `direccion_envio`, `telefono`, `fecha_registro`), buscar por email para login |
| **Admin. de Contenido**       | `administrador_contenido`, `admin_contenido_permisos` | `AdminContenidoDAO`        | Crear admin, listar/gestionar permisos                                                                 |
| **Admin. de Usuarios**        | `administrador_usuario`, `admin_usuario_permisos`     | `AdminUsuarioDAO`          | Crear admin, definir nivel/permisos                                                                    |
| **Desarrollador de Producto** | `desarrollador_producto`                              | `DesarrolladorProductoDAO` | Alta / listado                                                                                         |
| **Categorías**                | `categoria`                                           | `CategoriaDAO`             | Insertar, buscar por id, listar todas                                                                  |
| **Productos**                 | `producto` (FK `categoria_id`)                        | `ProductDAO`               | Insertar / actualizar, **listar con JOIN** a categoría                                                 |
| **Fábricas**                  | `fabrica`                                             | `FabricaDAO`               | Crear / listar                                                                                         |
| **Trabajadores**              | `trabajador`                                          | `TrabajadorDAO`            | Crear / listar, asignación a fábrica                                                                   |
| **Consejo Sombrío**           | `consejo_sombrio`                                     | `ConsejoSombrioDAO`        | Crear / renombrar / listar / buscar                                                                    |
| **Miembros del Consejo**      | `consejo_sombrio_miembro`                             | `ConsejoSombrioDAO`        | Agregar / retirar miembro, **listar miembros (JOIN `usuario`)**                                        |
| **Carrito**                   | `carrito`, `carrito_linea_item`                       | `CarritoDAO`               | Crear/obtener por cliente, agregar/eliminar ítems, total                                               |
| **Compras**                   | `compra`, `linea_compra`                              | —                          | **Pendiente** de conectar al checkout (tablas listas)                                                  |


<small>* Si no tienes aún esos DAO como clases separadas, la persistencia se maneja desde servicios o se encuentra en proceso; las tablas ya están listas en tu esquema.</small>

🧭 Cómo se usa desde el menú (enlazado a BD)

[1] Registrar usuario (Cliente) → UsuarioDAO.insertarCliente(...) (inserta con NOW() en fecha_registro).

[2] Log in → UsuarioDAO.buscarPorEmail(...).

[3] Agregar producto → lista categorías existentes, pide ID válido (CategoriaDAO.buscarPorId(...)) y guarda con ProductDAO.insertar(...) → rellena categoria_id.

[4] Catálogo → ProductDAO.listarConCategoria() (LEFT JOIN a categoria) para imprimir cat:<nombre>.

[7] Consejo Principal → crea si no existe (nombre_clave='Consejo Principal') y muestra miembros (JOIN usuario).

[8] Agregar miembro al Consejo → busca usuario por email, valida rol ADMIN_CONTENIDO/ADMIN_USUARIO, inserta en consejo_sombrio_miembro.

[10] Listar consejos → ConsejoSombrioDAO.listar().

[11] Gestionar consejo → ver/añadir/retirar miembros y renombrar.

🧱 Convenciones & Reglas de datos

Nombres de columnas (snake_case):
nombre_clave, consejo_id, usuario_id, fecha_registro, direccion_envio.
Mantener consistencia evita “Unknown column …”.

Roles (enum Rol): valores EXACTOS en mayúsculas
ADMIN_CONTENIDO, ADMIN_USUARIO, CLIENTE, DESARROLLADOR_PRODUCTO, DUENA, USUARIO_SIMPLE.
Si migras datos, normaliza con UPDATE.

Integridad referencial:

producto.categoria_id → categoria(id)

consejo_sombrio_miembro.(consejo_id,usuario_id) → consejo_sombrio/usuario

carrito_linea_item.carrito_id → carrito(id), ...producto_id → producto(id)

Duplicados: PK compuestas en tablas puente (consejo_sombrio_miembro, carrito_linea_item) evitan repetir pares. Maneja MySQL 1062 para dar mensaje amable.


<p align="center">
<img width="1280" height="300" alt="glowup-section-como-ejecutar-onyx" src="https://github.com/user-attachments/assets/fe78ff8f-17aa-4abe-a9dd-752dafae7dc2" />

## 🚀 Requisitos
- Java 17+ (JDK)
- IntelliJ IDEA (o cualquier IDE)

## ▶️ Cómo ejecutar
**Opción A – IntelliJ**
1. Abrir el proyecto.
2. Marcar `src/` como *Sources Root* (si hace falta).
3. Ejecutar `app.Main`.


---
