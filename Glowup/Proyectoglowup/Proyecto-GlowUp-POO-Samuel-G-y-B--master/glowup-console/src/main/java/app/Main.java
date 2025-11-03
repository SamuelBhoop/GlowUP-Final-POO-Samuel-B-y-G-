package app;
import dominio.Cliente;
import dominio.DesarrolladorProducto;
import dominio.Duena;
import dominio.UsuarioSimple;
import comercio.Compra;
import config.Database;
import servicios.*;
import dominio.Usuario;
import dominio.Cliente;
import dominio.Duena;
import dominio.AdministradorContenido;
import dominio.AdministradorUsuario;
import comercio.Carrito;
import produccion.Producto;
import comercio.Categoria;
import pago.MetodoPago;
import pago.TipoMetodoPago;
import produccion.Fabrica;
import produccion.*;
import operaciones.ConsejoSombrio;
import servicios.ConsejoSombrioDAO;
import servicios.MiembroConsejoSombrioDAO;
import config.Database;
import dominio.Usuario;
import dominio.Rol;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
public class Main {
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws SQLException {
        System.out.println("[DBG] user.dir=" + System.getProperty("user.dir"));
        System.out.println("[DBG] classpath=" + System.getProperty("java.class.path"));

        try (var c = Database.getConnection()) {
            System.out.println("[DB] URL=" + c.getMetaData().getURL()
                    + " Catalog=" + c.getCatalog()
                    + " AutoCommit=" + c.getAutoCommit());
        }
        AuthService auth = new AuthService();
        ProductService productos = new ProductService();
        CartService carritos = new CartService(productos);
        PurchaseService compras = new PurchaseService(productos);
        FabricaService fabricas = new FabricaService();
        TrabajadorService trabajadores = new TrabajadorService();
        ConsejoSombrioService consejos = new ConsejoSombrioService();
        RegistroEsclavosService registroSrv = new RegistroEsclavosService();

        Duena duena = seedData(auth, productos);
        ProduccionService produccion = new ProduccionService();
        OperacionesService operaciones = new OperacionesService(duena);

        Usuario usuarioActual = null;

        while (true) {
            System.out.println("\n==== GLOW UP (consola) ====");
            System.out.println("1) Registrar usuario (Cliente)");
            System.out.println("2) Log in");
            System.out.println("3) Agregar producto (AdminContenido/Dueña)");
            System.out.println("4) Listar productos");
            System.out.println("5) Agregar producto a mi carrito (Cliente)");
            System.out.println("6) Ver mi carrito");
            System.out.println("7) Registrar compra (checkout)");
            System.out.println("8) Módulo académico (Solo Dueña)");
            System.out.println("0) Salir");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            try {
                switch (op) {
                    case "1" -> registrarCliente(auth);
                    case "2" -> usuarioActual = login(auth);
                    case "3" -> agregarProducto(usuarioActual, productos);
                    case "4" -> listarProductos(productos);
                    case "5" -> agregarAlCarrito(usuarioActual, productos, carritos);
                    case "6" -> verCarrito(usuarioActual, carritos);
                    case "7" -> checkout(usuarioActual, carritos, compras);
                    case "8" -> moduloAcademico(usuarioActual, duena, produccion, operaciones, auth, fabricas, trabajadores, consejos, registroSrv);
                    case "0" -> {
                        System.out.println("\n¡Gracias por usar GlowUp! ¡Hasta pronto! ");
                        return;
                    }

                    default -> System.out.println("Opción no válida");
                }
            } catch (RuntimeException ex) {
                System.out.println("⚠ " + ex.getMessage());
            }
        }
    }

    private static void registrarCliente(AuthService auth) {
        System.out.println("-- Registro de Cliente --");
        System.out.print("Nombre: "); String nombre = sc.nextLine();
        System.out.print("Email: "); String email = sc.nextLine();
        System.out.print("Password: "); String pass = sc.nextLine();
        System.out.print("Dirección envío: "); String dir = sc.nextLine();
        System.out.print("Teléfono: "); String tel = sc.nextLine();


        Cliente c = new Cliente(
                UUID.randomUUID().toString(),
                nombre,
                email,
                pass,
                dir,
                tel
        );

        auth.registrar(c);
        System.out.println("✔ Cliente registrado");
    }

    private static Usuario login(AuthService auth) {
        System.out.println("-- Log in --");
        System.out.print("Email: "); String email = sc.nextLine();
        System.out.print("Password: "); String pass = sc.nextLine();
        Usuario u = auth.login(email, pass);
        System.out.println("✔ Bienvenido, " + u.getNombre() + " (" + u.getRol() + ")");
        return u;
    }

    private static void agregarProducto(Usuario u, ProductService productos) {
        if (u == null || !(u instanceof AdministradorContenido || u instanceof Duena)) {
            throw new RuntimeException("Debes iniciar sesión como AdminContenido o Dueña");
        }

        System.out.println("-- Agregar producto --");
        System.out.print("Nombre: "); String nombre = sc.nextLine();
        System.out.print("Descripción: "); String desc = sc.nextLine();
        System.out.print("Precio: "); double precio = Double.parseDouble(sc.nextLine());
        System.out.print("Stock: "); int stock = Integer.parseInt(sc.nextLine());

        CategoriaDAO catDao = new CategoriaDAO();
        Categoria categoria = pedirCategoriaExistente(sc, catDao);

        Producto p = new Producto(
                java.util.UUID.randomUUID().toString(),
                nombre, desc, precio, stock,
                java.time.LocalDate.now(),
                categoria
        );

        new ProductDAO().insertar(p);
        System.out.println("✔ Producto creado: " + p.getNombre() + " | ID: " + p.getId());
    }

    private static Categoria pedirCategoriaExistente(Scanner sc, CategoriaDAO catDao) {
        var cats = catDao.listarTodas();
        if (cats.isEmpty()) {
            throw new RuntimeException("No hay categorías creadas. Inserta alguna en la tabla 'categoria'.");
        }

        System.out.println("\nCategorías disponibles:");
        cats.forEach(c -> System.out.printf(" - %s | %s%n", c.getId(), c.getNombre()));

        while (true) {
            System.out.print("ID de la categoría a usar (o 'q' para cancelar): ");
            String catId = sc.nextLine().trim();
            if (catId.equalsIgnoreCase("q")) throw new RuntimeException("Operación cancelada.");
            var catOpt = catDao.buscarPorId(catId);
            if (catOpt.isPresent()) return catOpt.get();
            System.out.println("⚠ Categoría no existe. Intenta otra vez.");
        }
    }


    private static void listarProductos(ProductService productos) {
        System.out.println("-- Catálogo --");
        var lista = new ProductDAO().listarConCategoria();  // <- usa el JOIN
        lista.forEach(p -> System.out.println(p.resumen()));
    }

    private static void agregarAlCarrito(Usuario u, ProductService productos, CartService carritos) {
        if (!(u instanceof Cliente c)) {
            throw new RuntimeException("Debes iniciar sesión como Cliente");
        }
        System.out.print("ID producto: "); String id = sc.nextLine();
        System.out.print("Cantidad: "); int cant = Integer.parseInt(sc.nextLine());

        carritos.agregarProductoAlCarrito(c, id, cant);

        System.out.println("✔ Agregado. Subtotal actual: $" + carritos.obtenerTotalCarrito(c));
    }

    private static void verCarrito(Usuario u, CartService carritos) {
        if (!(u instanceof Cliente c)) throw new RuntimeException("Debes ser Cliente");
        Carrito carrito = carritos.obtenerCarrito(c);
        System.out.println("-- Mi carrito --");
        carrito.getLineas().forEach(System.out::println);
        System.out.println("TOTAL: $" + carrito.getTotal());
    }

    private static void checkout(Usuario u, CartService carritos, PurchaseService compras) {
        if (!(u instanceof Cliente c)) throw new RuntimeException("Debes ser Cliente");
        Carrito carrito = carritos.obtenerCarrito(c);
        if (carrito.getLineas().isEmpty()) throw new RuntimeException("Carrito vacío");
        MetodoPago mp = new MetodoPago(UUID.randomUUID().toString(), TipoMetodoPago.TARJETA,
                c.getNombre(), "****-****-****-1234");
        Compra compra = compras.checkout(c, carrito, mp);
        System.out.println("✔ Compra registrada #" + compra.getId() + ". Total $" + compra.getTotal());
    }

    private static Duena seedData(AuthService auth, ProductService prodSrv) {
        Duena duena = new Duena("duena-1", "Cabrita Sakura", "sakura@glowup.com", "1234", "CLAVE-ULTRA", LocalDate.now());
        AdministradorContenido editor = new AdministradorContenido("editor-1", "Editor", "editor@glowup.com", "1234", Set.of("CREATE","UPDATE","DELETE"));
        AdministradorUsuario adminUsr = new AdministradorUsuario("admin-usr-1", "AdminUsr", "adminusr@glowup.com", "1234", 3);

        auth.registrar(duena);
        auth.registrar(editor);
        auth.registrar(adminUsr);

        System.out.println("[SEED] Usuarios creados en memoria");
        return duena;
    }
    private static void moduloAcademico(Usuario actual, Duena duena, ProduccionService prodSrv, OperacionesService opSrv,
                                        AuthService auth, FabricaService fabricas, TrabajadorService trabajadores,
                                        ConsejoSombrioService consejos, RegistroEsclavosService registroSrv) {
        if (!(actual instanceof Duena)) {
            throw new RuntimeException("Acceso restringido: solo Dueña");
        }

        while (true) {
            System.out.println("\n-- Módulo académico AVANZADO --");
            System.out.println("=== FÁBRICAS Y TRABAJADORES ===");
            System.out.println("1) Crear fábrica");
            System.out.println("2) Listar fábricas");
            System.out.println("3) Registrar trabajador");
            System.out.println("4) Listar trabajadores");
            System.out.println("5) Asignar trabajador a fábrica");
            System.out.println("6) Abrir/crear RegistroEsclavos y gestionar");

            System.out.println("=== CONSEJO SOMBRÍO ===");
            System.out.println("7) Crear/Mostrar Consejo Principal");
            System.out.println("8) Agregar miembro al Consejo Principal");
            System.out.println("9) Crear nuevo Consejo Sombrio");
            System.out.println("10) Listar todos los Consejos");
            System.out.println("11) Gestionar Consejo específico");

            System.out.println("=== ADMINISTRADORES ===");
            System.out.println("12) Crear Administrador de Contenido");
            System.out.println("13) Crear Administrador de Usuarios");

            System.out.println("0) Volver");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();
            try {
                switch (op) {
                    case "1" -> crearFabrica(prodSrv);
                    case "2" -> listarFabricas(prodSrv);
                    case "3" -> registrarTrabajador(duena, opSrv, prodSrv);
                    case "4" -> listarTrabajadores(duena, opSrv);
                    case "5" -> asignarTrabajador(duena, prodSrv, opSrv);
                    case "6" -> gestionarRegistroEsclavos(duena, trabajadores, registroSrv);
                    case "7" -> mostrarConsejoPrincipal(opSrv);
                    case "8" -> agregarMiembroConsejoPrincipal(auth, opSrv);
                    case "9" -> crearNuevoConsejo(consejos);
                    case "10" -> listarTodosConsejos(consejos);
                    case "11" -> gestionarConsejoEspecifico(auth, consejos);
                    case "12" -> crearAdminContenido(auth);
                    case "13" -> crearAdminUsuario(auth);
                    case "0" -> {
                        System.out.println("\n️Volviendo al menú principal...");
                        return;
                    }

                    default -> System.out.println("Opción no válida");
                }
            } catch (Exception e) {
                System.out.println("⚠ " + e.getMessage());
            }
        }
    }

    private static void crearAdminUsuario(AuthService auth) {
        System.out.println("\n-- Crear Administrador de Usuarios --");

        String id = UUID.randomUUID().toString();
        System.out.println(" ID generado: " + id);

        System.out.print("Nombre: "); String nombre = sc.nextLine();
        System.out.print("Email: "); String email = sc.nextLine();
        System.out.print("Password: "); String password = sc.nextLine();
        System.out.print("Nivel de permisos (1-5): "); int nivelPermisos = Integer.parseInt(sc.nextLine());
        System.out.println("Ingresa los permisos (escribe cada permiso y presiona enter, escribe 'fin' para terminar):");
        Set<String> permisos = new HashSet<>();
        while (true) {
            System.out.print("Permiso: ");
            String permiso = sc.nextLine().trim();
            if (permiso.equalsIgnoreCase("fin")) {
                break;
            }
            if (!permiso.isEmpty()) {
                permisos.add(permiso.toUpperCase());
                System.out.println(" Permiso agregado: " + permiso.toUpperCase());
            }
        }

        AdministradorUsuario admin = new AdministradorUsuario(
                id,
                nombre,
                email,
                password,
                permisos,
                nivelPermisos,
                nivelPermisos >= 2
        );

        AdminUsuarioDAO dao = new AdminUsuarioDAO();
        dao.insertar(admin);

        System.out.println(" Administrador de Usuarios creado exitosamente!");
        System.out.println(" Resumen: " + nombre + " | Nivel: " + nivelPermisos + " | Permisos: " + permisos.size());
        System.out.println(" Guardado en base de datos");
    }
    private static void crearFabrica(ProduccionService prodSrv) {

        String id = UUID.randomUUID().toString();
        System.out.print("País: "); String pais = sc.nextLine();
        System.out.print("Ciudad: "); String ciudad = sc.nextLine();
        System.out.print("Capacidad: "); int cap = Integer.parseInt(sc.nextLine());
        System.out.print("Nivel automatización: "); String nivel = sc.nextLine();

        Fabrica f = prodSrv.crearFabrica(id, pais, ciudad, cap, nivel);
        System.out.println("✔ Creada fábrica ID: " + f.getId());
    }

    private static void listarFabricas(ProduccionService prodSrv) {
        System.out.println("-- Fábricas --");
        prodSrv.listar().forEach(System.out::println);
    }

    private static void registrarTrabajador(Duena duena, OperacionesService opSrv, ProduccionService prodSrv) {
        System.out.print("Nombre: "); String nombre = sc.nextLine();
        System.out.print("País origen: "); String pais = sc.nextLine();
        System.out.print("Edad: "); int edad = Integer.parseInt(sc.nextLine());
        System.out.print("Salud: "); String salud = sc.nextLine();


        System.out.print("¿Asignar a fábrica? (s/n): ");
        String asignar = sc.nextLine().trim().toLowerCase();

        Fabrica fabrica = null;
        if (asignar.equals("s") || asignar.equals("si")) {
            System.out.print("ID fábrica: "); String fid = sc.nextLine();
            fabrica = prodSrv.get(fid);
        }

        var t = opSrv.registrarTrabajador(duena, nombre, pais, edad, LocalDate.now(), salud);


        if (fabrica != null) {
            opSrv.asignarAFabrica(duena, t.getId(), fabrica);
        }

        System.out.println("✔ Registrado trabajador id=" + t.getId());
    }

    private static void listarTrabajadores(Duena duena, OperacionesService opSrv) {
        System.out.println("-- Trabajadores --");
        opSrv.listarTrabajadores(duena).forEach(System.out::println);
    }

    private static void asignarTrabajador(Duena duena, ProduccionService prodSrv, OperacionesService opSrv) {
        System.out.println("-- Trabajadores --");
        opSrv.listarTrabajadores(duena).forEach(t ->
                System.out.println("ID: " + t.getId() + " | " + t.getNombre())
        );

        System.out.println("-- Fábricas --");
        prodSrv.listar().forEach(f ->
                System.out.println("ID: " + f.getId() + " | " + f.getCiudad() + ", " + f.getPais())
        );

        System.out.print("ID trabajador: "); String tid = sc.nextLine();
        System.out.print("ID fábrica: "); String fid = sc.nextLine();
        opSrv.asignarAFabrica(duena, tid, prodSrv.get(fid));
        System.out.println("✔ Asignación realizada");
    }
    private static void mostrarConsejo(OperacionesService opSrv) {
        var c = opSrv.crearConsejoSiNoExiste("cns-1", "Sombras");
        System.out.println("\n=== CONSEJO SOMBRÍO ===");
        System.out.println("ID: " + c.getId());
        System.out.println("Nombre Clave: " + c.getNombreClave());
        System.out.println("Miembros: " + c.getMiembros().size());

        if (c.getMiembros().isEmpty()) {
            System.out.println("No hay miembros en el consejo");
        } else {
            System.out.println("\n--- MIEMBROS ---");
            c.getMiembros().forEach(miembro ->
                    System.out.println("• " + miembro.getNombre() + " (" + miembro.getEmail() + ") - " + miembro.getRol())
            );
        }
        System.out.println("=====================");
    }

    private static void agregarMiembroConsejo(AuthService auth, OperacionesService opSrv) {
        var c = opSrv.crearConsejoSiNoExiste("cns-1", "Sombras");

        System.out.println("\n-- Agregar Miembro al Consejo --");
        System.out.print("Email del miembro: ");
        String email = sc.nextLine();

        try {
            Usuario miembro = auth.buscarPorEmail(email);
            if (miembro == null) {
                System.out.println(" Usuario no encontrado con email: " + email);
                return;
            }

            if (!(miembro instanceof AdministradorContenido) && !(miembro instanceof AdministradorUsuario)) {
                System.out.println(" Solo Administradores de Contenido o Usuarios pueden ser miembros");
                System.out.println("   Rol actual: " + miembro.getRol());
                return;
            }

            c.agregarMiembro(miembro);
            System.out.println(" Miembro agregado: " + miembro.getNombre() + " (" + miembro.getRol() + ")");

        } catch (Exception e) {
            System.out.println(" Error: " + e.getMessage());
        }
    }

    private static void seedOperaciones(AuthService auth,
                                        FabricaService fabricas,
                                        TrabajadorService trabajadores,
                                        ConsejoSombrioService consejos) {
        var f1 = fabricas.crear(UUID.randomUUID().toString(), "Colombia", "Medellín", 100, "Alta");

        var t1 = trabajadores.crear(UUID.randomUUID().toString(), "Joao", "Brasil", 28, java.time.LocalDate.now(), "Estable");
        var t2 = trabajadores.crear(UUID.randomUUID().toString(), "Luis", "Colombia", 32, java.time.LocalDate.now(), "Observación");
        fabricas.asignar(f1.getId(), t1);

        auth.registrar(new AdministradorUsuario("adminusr-1", "AdminUsr", "adminusr@glowup.com", "1234", 3));

        var consejo = consejos.crear(UUID.randomUUID().toString(), "Noche-Alpha");
        var editor = auth.buscarPorEmail("editor@glowup.com");
        var admUsr = auth.buscarPorEmail("adminusr@glowup.com");
        if (editor != null) ((operaciones.ConsejoSombrio) consejo).agregarMiembro(editor);
        if (admUsr != null) ((operaciones.ConsejoSombrio) consejo).agregarMiembro(admUsr);
    }
    private static void gestionarRegistroEsclavos(Duena duena, TrabajadorService trabajadores, RegistroEsclavosService registroSrv) {
        System.out.println("\n-- Gestión de Registro de Esclavos --");
    }

    private static void mostrarConsejoPrincipal(OperacionesService opSrv) {
        ConsejoSombrioDAO dao = new ConsejoSombrioDAO();
        ConsejoSombrio principal = getOrCreateConsejoPrincipal(dao);

        System.out.println("\n-- Consejo Principal --");
        System.out.println("ID: " + principal.getId());
        System.out.println("Nombre clave: " + principal.getNombreClave());

        var miembros = dao.listarMiembros(principal.getId());
        System.out.println("Miembros (" + miembros.size() + "):");
        for (Usuario u : miembros) {
            System.out.printf(" - %s | %s | %s%n", u.getId(), u.getNombre(), u.getRol());
        }


         opSrv.crearConsejoSiNoExiste(principal.getId(), principal.getNombreClave());
    }

    private static void agregarMiembroConsejoPrincipal(AuthService auth, OperacionesService opSrv) {
        ConsejoSombrioDAO dao = new ConsejoSombrioDAO();
        ConsejoSombrio principal = getOrCreateConsejoPrincipal(dao);

        System.out.println("\n-- Agregar Miembro al Consejo Principal --");
        System.out.print("Email del usuario (debe ser Admin): ");
        String email = sc.nextLine().trim();

        String usuarioId = buscarUsuarioAdminIdPorEmail(email);
        if (usuarioId == null) {
            System.out.println("⚠ No existe un usuario con ese email.");
            return;
        }

        if (dao.agregarMiembro(principal.getId(), usuarioId)) {
            System.out.println("✔ Miembro agregado al Consejo Principal.");
        } else {
            System.out.println("⚠ No se pudo agregar el miembro.");
        }
    }


    private static void crearNuevoConsejo(ConsejoSombrioService consejos) {
        ConsejoSombrioDAO dao = new ConsejoSombrioDAO();
        System.out.println("\n-- Crear Nuevo Consejo --");
        System.out.print("Nombre clave: ");
        String nombre = sc.nextLine().trim();
        if (nombre.isEmpty()) {
            System.out.println("⚠ Nombre inválido.");
            return;
        }
        ConsejoSombrio c = dao.crear(nombre);
        System.out.println("✔ Consejo creado. ID: " + c.getId());


         consejos.crear(c.getId(), c.getNombreClave());
    }

    private static void listarTodosConsejos(ConsejoSombrioService consejos) {
        ConsejoSombrioDAO dao = new ConsejoSombrioDAO();
        System.out.println("\n-- Consejos --");
        var lista = dao.listar();
        if (lista.isEmpty()) {
            System.out.println("(sin registros)");
            return;
        }
        for (ConsejoSombrio c : lista) {
            int miembros = dao.listarMiembros(c.getId()).size();
            System.out.printf(" - %s | %s | miembros: %d%n", c.getId(), c.getNombreClave(), miembros);
        }
    }

    private static void gestionarConsejoEspecifico(AuthService auth, ConsejoSombrioService consejos) {
        ConsejoSombrioDAO dao = new ConsejoSombrioDAO();
        System.out.println("\n-- Gestionar Consejo Específico --");

        var todos = dao.listar();
        if (todos.isEmpty()) {
            System.out.println("No hay consejos. Crea uno primero.");
            return;
        }
        for (int i = 0; i < todos.size(); i++) {
            System.out.printf("%d) %s | %s%n", i + 1, todos.get(i).getId(), todos.get(i).getNombreClave());
        }
        System.out.print("Seleccione #: ");
        String sel = sc.nextLine().trim();
        int idx;
        try { idx = Integer.parseInt(sel) - 1; } catch (Exception e) { System.out.println("Opción inválida."); return; }
        if (idx < 0 || idx >= todos.size()) { System.out.println("Opción inválida."); return; }

        ConsejoSombrio actual = todos.get(idx);

        while (true) {
            System.out.println("\n-- Consejo: " + actual.getNombreClave() + " --");
            System.out.println("1) Ver miembros");
            System.out.println("2) Agregar miembro (email admin)");
            System.out.println("3) Retirar miembro (email admin)");
            System.out.println("4) Renombrar consejo");
            System.out.println("0) Volver");
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            switch (op) {
                case "1" -> {
                    var miembros = dao.listarMiembros(actual.getId());
                    if (miembros.isEmpty()) System.out.println("(sin miembros)");
                    for (Usuario u : miembros) {
                        System.out.printf(" - %s | %s | %s%n", u.getId(), u.getNombre(), u.getRol());
                    }
                }
                case "2" -> {
                    System.out.print("Email del admin a agregar: ");
                    String email = sc.nextLine().trim();
                    String usuarioId = buscarUsuarioAdminIdPorEmail(email);
                    if (usuarioId == null) { System.out.println("⚠ Usuario no existe."); break; }
                    boolean ok = dao.agregarMiembro(actual.getId(), usuarioId);
                    System.out.println(ok ? "✔ Agregado" : "⚠ No se pudo agregar");
                }
                case "3" -> {
                    System.out.print("Email del admin a retirar: ");
                    String email = sc.nextLine().trim();
                    String usuarioId = buscarUsuarioAdminIdPorEmail(email);
                    if (usuarioId == null) { System.out.println("⚠ Usuario no existe."); break; }
                    boolean ok = dao.retirarMiembro(actual.getId(), usuarioId);
                    System.out.println(ok ? "✔ Retirado" : "⚠ No se pudo retirar");
                }
                case "4" -> {
                    System.out.print("Nuevo nombre clave: ");
                    String nuevo = sc.nextLine().trim();
                    if (nuevo.isEmpty()) { System.out.println("⚠ Nombre inválido."); break; }
                    boolean ok = dao.actualizarNombre(actual.getId(), nuevo);
                    if (ok) {
                        System.out.println("✔ Renombrado");
                        actual = dao.buscarPorId(actual.getId()).orElse(actual);
                    } else System.out.println("⚠ No se pudo renombrar");
                }
                case "0" -> { return; }
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private static void crearAdminContenido(AuthService auth) {
        System.out.println("\n-- Crear Administrador de Contenido --");
    }
    private static ConsejoSombrio getOrCreateConsejoPrincipal(ConsejoSombrioDAO dao) {
        for (ConsejoSombrio c : dao.listar()) {
            if ("Consejo Principal".equalsIgnoreCase(c.getNombreClave())) return c;
        }
        ConsejoSombrio nuevo = dao.crear("Consejo Principal");
        System.out.println("✔ Consejo Principal creado. ID: " + nuevo.getId());
        return nuevo;
    }

    private static String buscarUsuarioAdminIdPorEmail(String email) {
        String sql = "SELECT id, rol FROM usuario WHERE email = ?";
        try (var cn = Database.getConnection();
             var ps = cn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String id = rs.getString("id");
                String rol = rs.getString("rol");
                if (!"ADMIN_CONTENIDO".equals(rol) && !"ADMIN_USUARIO".equals(rol)) {
                    throw new RuntimeException("El usuario no es ADMIN_CONTENIDO ni ADMIN_USUARIO.");
                }
                return id;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error consultando usuario por email: " + e.getMessage(), e);
        }
    }


}