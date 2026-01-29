package Backend.Logica;

import Backend.Repositorios.*;
import Backend.Seguridad.*;
import Backend.Clases.*;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.StreamSupport;

public class Main {
    static Scanner entrada = new Scanner(System.in);

    static RepositorioUsuario repoUsuarios; // solo declaramos
    static RepositorioPizza repoPizza ;
    static RepositorioPedido repoPedido ;

    // ========== ARRAYS DE VALIDACIÓN ==========
    static final String[] TAMANIOS = {"Pequeña", "Mediana", "Grande"};
    static final String[] MASAS = {"Fina", "Gruesa"};
    static final String[] PUESTOS = {"Gerente", "Cocinero", "Encargado", "Atencion al cliente"};
    static final String[] TURNOS = {"Mañana", "Tarde", "Rotativo"};
    static final String[] METODOS_PAGO = {"Bizum", "Efectivo"};
    static final String[] TIPOS_ENTREGA = {"Domicilio", "Recogida"};

    static Connection obtenerConexion() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/pizzeria"; // Cambia tu DB
        String usuario = "root";                           // Cambia tu usuario
        String password = "123";                         // Cambia tu contraseña
        return DriverManager.getConnection(url, usuario, password);
    }

    public static void main(String[] args) throws SQLException {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║  PIZZERIA RACHANA Y ALEJANDRO      ║");
        System.out.println("╚════════════════════════════════════╝\n");

        // Borrar todos los datos NO LO USAMOS
       /* repoUsuarios.deleteAll();
        repoPedido.deleteAll();*/

        inicio();
        entrada.close();
    }

    // ===================== MÉTODO DE VALIDACIÓN REUTILIZABLE =====================
    static String validarOpcion(String prompt, String[] opcionesValidas) {
        String entrada_valor = "";
        boolean esValida = false;

        while (!esValida) {
            System.out.print(prompt);
            entrada_valor = entrada.nextLine().trim();

            final String input = entrada_valor;
            esValida = Arrays.stream(opcionesValidas)
                    .anyMatch(opcion -> opcion.equalsIgnoreCase(input));

            if (!esValida) {
                System.err.println("Debes escribir una de estas opciones: " +
                        String.join(" / ", opcionesValidas));
            }
        }

        return entrada_valor;
    }

    // ===================== MENÚ PRINCIPAL =====================
    static void inicio() throws SQLException {
        int opcion;
        do {
            System.out.println("\n========== MENU PRINCIPAL ==========");
            System.out.println("1. Iniciar Sesion");
            System.out.println("2. Crear cuenta");
            System.out.println("3. Hacer pedido sin cuenta");
            System.out.print("Opcion: ");

            try {
                opcion = entrada.nextInt();
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                continue;
            }

            switch(opcion) {
                case 1 -> iniciarSesion();
                case 2 -> registrarUsuario();
                case 3 -> pedidoInvitado();
                default -> System.err.println("Opcion invalida");
            }
        } while (true);
    }

    // ===================== PEDIDO INVITADO =====================
    static void pedidoInvitado() throws SQLException {
        RepositorioUsuario repoUsuarios=new RepositorioUsuario(obtenerConexion());
        System.out.println("\n========== PEDIDO SIN CUENTA ==========");

        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();

        System.out.print("Apellidos: ");
        String apellidos = entrada.nextLine();

        System.out.print("Telefono: ");
        int telefono = 0;
        boolean telefonoValido = false;
        while (!telefonoValido) {
            try {
                telefono = entrada.nextInt();
                telefonoValido = true;
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Telefono: ");
            }
        }

        System.out.print("Email: ");
        String email = entrada.nextLine();

        System.out.print("Tarjeta de credito: ");
        int tarjeta = 0;
        boolean tarjetaValida = false;
        while (!tarjetaValida) {
            try {
                tarjeta = entrada.nextInt();
                tarjetaValida = true;
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Tarjeta de credito: ");
            }
        }

        System.out.print("Direccion: ");
        String direccion = entrada.nextLine();

        Cliente usuarioInvitado = new Cliente(nombre, apellidos, telefono,email,tarjeta,direccion);
        usuarioInvitado.setDireccion(direccion);
        repoUsuarios.save(usuarioInvitado);

        realizarPedido(usuarioInvitado);
    }

    //Hecho
    // ===================== REGISTRO =====================
    static void registrarUsuario() throws SQLException {
        System.out.println("\n========== REGISTRO ==========");

        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();

        System.out.print("Apellidos: ");
        String apellidos = entrada.nextLine();

        System.out.print("Telefono: ");
        int telefono = 0;
        boolean telefonoValido = false;
        while (!telefonoValido) {
            try {
                telefono = entrada.nextInt();
                telefonoValido = true;
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Telefono: ");
            }
        }

        System.out.print("Email: ");
        String email = entrada.nextLine();

        if (email.endsWith("@pizzeria")) {
            registrarTrabajador(nombre, apellidos, telefono, email);
        } else {
            registrarCliente(nombre, apellidos, telefono, email);
        }
    }

    //Hecho
    static void registrarCliente(String nombre, String apellidos, int telefono, String email) throws SQLException {
        RepositorioUsuario repoUsuarios = new RepositorioUsuario(obtenerConexion());

        System.out.print("Tarjeta de credito: ");
        int tarjetaCredito = 0;
        boolean tarjetaValida = false;
        while (!tarjetaValida) {
            try {
                tarjetaCredito = entrada.nextInt();
                tarjetaValida = true;
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Tarjeta de credito: ");
            }
        }

        System.out.print("Direccion: ");
        String direccion = entrada.nextLine();

        System.out.print("Contraseña: ");
        String contrasena = entrada.nextLine();

        System.out.print("Repite contraseña: ");
        String repetirContrasena = entrada.nextLine();

        if (!contrasena.equals(repetirContrasena)) {
            System.err.println("Las contraseñas no coinciden.");
            return;
        }

        String contrasenaCifrada = SimpleBCrypt.hash(contrasena);
        Cliente cliente = new Cliente(nombre, apellidos, telefono, email, tarjetaCredito, direccion, contrasenaCifrada);
        repoUsuarios.save(cliente);

        System.out.println("Cuenta de cliente creada.");
    }

    //Hecho
    static void registrarTrabajador(String nombre, String apellidos, int telefono, String email) throws SQLException {
        RepositorioUsuario repoUsuarios = new RepositorioUsuario(obtenerConexion());
        String puesto = validarOpcion("Puesto (" + String.join("/", PUESTOS) + "): ", PUESTOS);

        System.out.print("Salario: ");
        double salario = 0;
        boolean salarioValido = false;
        while (!salarioValido) {
            try {
                salario = entrada.nextDouble();
                salarioValido = true;
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Salario: ");
            }
        }

        String turno = validarOpcion("Turno (" + String.join("/", TURNOS) + "): ", TURNOS);

        // AÑADE ESTO:
        System.out.print("Dirección: ");
        String direccion = entrada.nextLine();

        System.out.print("Contraseña: ");
        String contrasena = entrada.nextLine();

        System.out.print("Repite contraseña: ");
        String repetirContrasena = entrada.nextLine();

        if (!contrasena.equals(repetirContrasena)) {
            System.err.println("Las contraseñas no coinciden.");
            return;
        }

        String contrasenaCifrada = SimpleBCrypt.hash(contrasena);
        Trabajador trabajador = new Trabajador(nombre, apellidos, telefono, email, puesto, salario, turno, contrasenaCifrada);
        trabajador.setDireccion(direccion);  // AÑADE ESTO TAMBIÉN
        repoUsuarios.save(trabajador);

        System.out.println("Cuenta de trabajador creada.");
    }

    //Hecho
    // ===================== INICIO DE SESIÓN - USA findAll() =====
    static void iniciarSesion() throws SQLException {
        RepositorioUsuario repoUsuarios=new RepositorioUsuario(obtenerConexion());
        System.out.println("\n========== INICIO DE SESION ==========");

        System.out.print("Email: ");
        String email = entrada.nextLine();

        System.out.print("Contraseña: ");
        String contrasena = entrada.nextLine();

        // USAR findAll() DE IRepositorio
        Iterable<Usuario> usuarios = repoUsuarios.findAll();

        for (Usuario u : usuarios) {
            if (u.getEmail().equals(email) && u.getContrasena() != null) {
                if (SimpleBCrypt.verify(contrasena, u.getContrasena())) {
                    System.out.println("Bienvenido " + u.getNombre());

                    if (u instanceof Cliente) {
                        menuCliente((Cliente) u);
                    } else if (u instanceof Trabajador) {
                        menuTrabajador((Trabajador) u);
                    }
                    return;
                }
            }
        }

        System.err.println("Usuario o contrasena incorrectos.");
    }

    // ===================== MENÚ CLIENTE =====================
    static void menuCliente(Cliente cliente) throws SQLException {
        int opcion = 0;
        do {
            System.out.println("\n========== MENU CLIENTE ==========");
            System.out.println("1. Modificar datos");
            System.out.println("2. Realizar pedido");
            System.out.println("3. Ver mis pedidos");
            System.out.println("4. Salir");
            System.out.print("Opcion: ");

            try {
                opcion = entrada.nextInt();
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                continue;
            }

            switch(opcion) {
                case 1 -> modificarDatosCliente(cliente, repoUsuarios);
                case 2 -> realizarPedido(cliente);
                case 3 -> verMisPedidos(cliente);
                case 4 -> System.out.println("Cerrando sesion...");
                default -> System.out.println("Opcion invalida");
            }
        } while(opcion != 4);
    }

    // ===================== MENÚ TRABAJADOR =====================
    static void menuTrabajador(Trabajador trabajador) throws SQLException {
        int opcion = 0;
        do {
            System.out.println("\n========== MENU TRABAJADOR ==========");
            System.out.println("1. Ver pedidos");
            System.out.println("2. Ver clientes");
            System.out.println("3. Añadir pizza");
            System.out.println("4. Añadir cliente");
            System.out.println("5. Añadir trabajador");
            System.out.println("6. Modificar datos");
            System.out.println("7. Realizar pedido");
            System.out.println("8. Eliminar pedido");
            System.out.println("9. Eliminar pizza");
            System.out.println("10. Salir");
            System.out.print("Opcion: ");

            try {
                opcion = entrada.nextInt();
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                continue;
            }

            switch(opcion) {
                case 1 -> verTodosPedidos();
                case 2 -> verClientes();
                case 3 -> aniadirPizza();
                case 4 -> aniadirClienteDesdeAdmin();
                case 5 -> aniadirTrabajador();
                case 6 -> modificarDatosTrabajador(trabajador, repoUsuarios);
                case 7 -> realizarPedido(trabajador);
                case 8 -> eliminarPedido();
                case 9 -> eliminarPizza();
                case 10 -> System.out.println("Cerrando sesion...");
                default -> System.err.println("Opcion invalida");
            }
        } while (opcion != 10);
    }

    // ===================== REALIZAR PEDIDO - USA findAll() =====
    static void realizarPedido(Usuario usuario) throws SQLException {
        RepositorioPizza repoPizza =new RepositorioPizza(obtenerConexion());

        System.out.println("\n========== PIZZAS DISPONIBLES ==========");

        // USAR findAll() DE IRepositorio
        List<Pizza> pizzasDisponibles = StreamSupport.stream(
                        repoPizza.findAll().spliterator(), false)
                .filter(Pizza::getDisponible)
                .toList();

        if (pizzasDisponibles.isEmpty()) {
            System.out.println("No hay pizzas disponibles.");
            return;
        }

        for (Pizza p : pizzasDisponibles) {
            System.out.println(p.getId() + ". " + p.getNombre() +
                    " - $" + p.getPrecio() + " (" + p.getDescripcion() + ")");
        }

        System.out.print("\nElige el numero de pizza (o 0 para personalizar): ");
        int idPizza = 0;
        boolean pizzaValida = false;
        while (!pizzaValida) {
            try {
                idPizza = entrada.nextInt();
                pizzaValida = true;
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Elige el numero de pizza (o 0 para personalizar): ");
            }
        }

        Pizza pizzaSeleccionada;

        if (idPizza == 0) {
            pizzaSeleccionada = crearPizzaPersonalizada();
        } else {
            // USAR findById() DE IRepositorio
            pizzaSeleccionada = repoPizza.findById(idPizza);
            if (pizzaSeleccionada == null) {
                System.err.println("Pizza no encontrada.");
                return;
            }
        }

        System.out.print("Cuantas pizzas?: ");
        int cantidad = 0;
        boolean cantidadValida = false;
        while (!cantidadValida) {
            try {
                cantidad = entrada.nextInt();
                cantidadValida = true;
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Cuantas pizzas?: ");
            }
        }

        Map<Pizza, Integer> cantidadPizzas = new HashMap<>();
        cantidadPizzas.put(pizzaSeleccionada, cantidad);

        crearPedido(usuario, cantidadPizzas, pizzaSeleccionada.getTiempoPreparacion());
    }

    // ===================== CREAR PIZZA PERSONALIZADA - USA count() =====
    static Pizza crearPizzaPersonalizada() {
        System.out.println("\n========== PIZZA PERSONALIZADA ==========");

        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();

        String tamano = validarOpcion("Tamaño (" + String.join("/", TAMANIOS) + "): ", TAMANIOS);
        String tipoMasa = validarOpcion("Tipo de masa (" + String.join("/", MASAS) + "): ", MASAS);

        System.out.print("Tipo de salsa: ");
        String tipoSalsa = entrada.nextLine();

        System.out.print("Ingredientes (separados por coma): ");
        String ingredientesStr = entrada.nextLine();
        List<String> ingredientes = Arrays.asList(ingredientesStr.split(","));

        // Calcular precio automaticamente
        double precio = 8.50;

        // Precio segun tamanno
        if (tamano.equalsIgnoreCase("Grande")) {
            precio += 2.00;
        } else if (tamano.equalsIgnoreCase("Mediana")) {
            precio += 1.00;
        }

        // Precio por ingrediente adicional
        double precioIngredientes = ingredientes.size() * 1.50;

        double precioTotal = precio + precioIngredientes;

        System.out.println("Precio calculado: $" + String.format("%.2f", precioTotal));

        // USAR count() DE IRepositorio
        int nuevoId = (int) (repoPizza.count() + 1);

        return new Pizza(nuevoId, nombre, "Pizza personalizada", precioTotal, true,
                tamano, tipoMasa, tipoSalsa, ingredientes, 20);
    }

    // ===================== CREAR PEDIDO - USA save() =====
    static void crearPedido(Usuario usuario, Map<Pizza, Integer> cantidadPizzas, int tiempoPreparacion) throws SQLException {
        RepositorioPedido repoPedido =new RepositorioPedido(obtenerConexion());
        LocalDateTime ahora = LocalDateTime.now();
        String numeroPedido = "PED-" + String.format("%03d", (int)(Math.random() * 1000)) + "-" + ahora.getYear();

        Date fechaPedido = Date.from(ahora.atZone(ZoneId.systemDefault()).toInstant());
        Date fechaEntrega = Date.from(ahora.plusMinutes(tiempoPreparacion).atZone(ZoneId.systemDefault()).toInstant());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime pedidoLD = Instant.ofEpochMilli(fechaPedido.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime entregaLD = Instant.ofEpochMilli(fechaEntrega.getTime())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        System.out.println("\nFecha pedido: " + pedidoLD.format(formatter));
        System.out.println("Entrega estimada: " + entregaLD.format(formatter));

        double subtotal = 0.0;
        for (Map.Entry<Pizza, Integer> entry : cantidadPizzas.entrySet()) {
            subtotal += entry.getKey().getPrecio() * entry.getValue();
        }

        double descuento = 0;

        if (usuario instanceof Cliente) {
            descuento = 10.0;
        } else if (usuario instanceof Trabajador) {
            descuento = 50.0;
        }

        double total = subtotal - (subtotal * descuento / 100);

        System.out.println("Subtotal: $" + String.format("%.2f", subtotal));
        System.out.println("Descuento: " + descuento + "%");
        System.out.println("Total: $" + String.format("%.2f", total));

        entrada.nextLine();
        String metodoPago = validarOpcion("Metodo de pago (" + String.join("/", METODOS_PAGO) + "): ", METODOS_PAGO);
        if (metodoPago.equalsIgnoreCase("Bizum") && !(usuario instanceof Cliente)) {
            System.out.print("Número de tarjeta: ");
            int tarjeta = 0;
            try {
                tarjeta = entrada.nextInt();
                entrada.nextLine();
                // Aquí podrías guardar la tarjeta si quieres
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
            }
        }
        String tipoEntrega = validarOpcion("Tipo de entrega (" + String.join("/", TIPOS_ENTREGA) + "): ", TIPOS_ENTREGA);

        String direccion;

        if (tipoEntrega.equalsIgnoreCase("Domicilio")) {
            if (usuario instanceof Cliente) {
                direccion = usuario.getDireccion();  // ← USA LA DE LA CLASE PADRE DIRECTAMENTE
            } else if (usuario.getDireccion() != null) {
                direccion = usuario.getDireccion();
            } else {
                System.out.print("Direccion de entrega: ");
                direccion = entrada.nextLine();
            }
        } else {
            direccion = "Recogida en tienda";
        }


        System.out.print("Notas adicionales (opcional): ");
        String notas = entrada.nextLine();
        if (notas.isEmpty()) notas = null;

        Pedido pedido = new Pedido(0, numeroPedido, "PENDIENTE", fechaPedido, fechaEntrega,
                usuario, cantidadPizzas, subtotal, descuento, total,
                metodoPago, true, tipoEntrega, direccion, notas);

        // USAR save() DE IRepositorio
        repoPedido.save(pedido);
        System.out.println("\nPedido creado: " + numeroPedido);
    }

    // ===================== VER MIS PEDIDOS =====
    static void verMisPedidos(Cliente cliente) throws SQLException {
        RepositorioPedido repoPedido = new RepositorioPedido(obtenerConexion());
        List<Pedido> pedidos = repoPedido.buscarPedidosPorClienteId(cliente.getId());

        if (pedidos.isEmpty()) {
            System.out.println("No tienes pedidos.");
        } else {
            System.out.println("\nTus pedidos:");
            for (Pedido p : pedidos) {
                System.out.println("  - " + p.getNumeroPedido() +
                        " | " + p.getEstado() +
                        " | $" + p.getTotal());
            }
        }
    }

    // ===================== VER TODOS LOS PEDIDOS - USA findAllToList() =====
    static void verTodosPedidos() throws SQLException {
        RepositorioPedido repoPedido = new RepositorioPedido(obtenerConexion());
        // USAR findAllToList() DE IRepositorioExtend
        List<Pedido> pedidos = repoPedido.findAllToList();

        if (pedidos.isEmpty()) {
            System.err.println("No hay pedidos.");
        } else {
            System.out.println("\nTodos los pedidos:");
            for (Pedido p : pedidos) {
                System.out.println("  - " + p.getNumeroPedido() +
                        " | Usuario: " + (p.getUsuario() != null ? p.getUsuario().getNombre() : "Desconocido") +
                        " | " + p.getEstado() +
                        " | $" + p.getTotal());
            }
        }
    }

    // ===================== VER CLIENTES - USA findAll() Y findAllToList() =====
    static void verClientes() {
        // USAR findAllToList() DE IRepositorioExtend
        List<Usuario> usuariosTotal = repoUsuarios.findAllToList();
        List<Cliente> clientes = usuariosTotal.stream()
                .filter(u -> u instanceof Cliente)
                .map(u -> (Cliente) u)
                .toList();

        if (clientes.isEmpty()) {
            System.err.println("No hay clientes registrados.");
        } else {
            System.out.println("\nClientes registrados:");
            for (Cliente c : clientes) {
                System.out.println("  - " + c.getNombre() + " " + c.getApellidos() +
                        " | " + c.getEmail() +
                        " | Tel: " + c.getTelefono());
            }
        }
    }

    // ===================== AÑADIR PIZZA - USA save() Y count() =====
    static void aniadirPizza() {
        System.out.println("\n========== AÑADIR PIZZA AL MENU ==========");

        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();

        System.out.print("Descripción: ");
        String descripcion = entrada.nextLine();

        String tamano = validarOpcion("Tamaño (" + String.join("/", TAMANIOS) + "): ", TAMANIOS);
        String tipoMasa = validarOpcion("Tipo de masa (" + String.join("/", MASAS) + "): ", MASAS);

        System.out.print("Tipo de salsa: ");
        String tipoSalsa = entrada.nextLine();

        System.out.print("Ingredientes (separados por coma): ");
        String ingredientesStr = entrada.nextLine();
        List<String> ingredientes = Arrays.asList(ingredientesStr.split(","));

        System.out.print("Precio: $");
        double precio = 0;
        boolean precioValido = false;
        while (!precioValido) {
            try {
                precio = entrada.nextDouble();
                precioValido = true;
                entrada.nextLine();
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Precio: $");
            }
        }

        System.out.print("Tiempo de preparación (minutos): ");
        int tiempoPreparacion = 0;
        boolean tiempoValido = false;
        while (!tiempoValido) {
            try {
                tiempoPreparacion = entrada.nextInt();
                entrada.nextLine();
                tiempoValido = true;
            } catch (InputMismatchException e) {
                System.err.println("Solo puedes escribir numeros");
                entrada.nextLine();
                System.out.print("Tiempo de preparación (minutos): ");
            }
        }

        // USAR count() DE IRepositorio
        int nuevoId = (int) (repoPizza.count() + 1);
        Pizza nuevaPizza = new Pizza(nuevoId, nombre, descripcion, precio, true,
                tamano, tipoMasa, tipoSalsa, ingredientes, tiempoPreparacion);

        // USAR save() DE IRepositorio
        repoPizza.save(nuevaPizza);
        System.out.println("Pizza añadida al menu.");
    }

    // ===================== AÑADIR CLIENTE (ADMIN) - USA save() Y existsById() =====
    static void aniadirClienteDesdeAdmin() {
        System.out.println("\n========== AÑADIR CLIENTE ==========");

        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();

        System.out.print("Apellidos: ");
        String apellidos = entrada.nextLine();

        System.out.print("Telefono: ");
        int telefono = 0;
        try {
            telefono = entrada.nextInt();
            entrada.nextLine();
        } catch (InputMismatchException e) {
            System.err.println("Solo puedes escribir numeros");
            entrada.nextLine();
            return;
        }

        System.out.print("Email: ");
        String email = entrada.nextLine();

        System.out.print("Tarjeta de credito: ");
        int tarjeta = 0;
        try {
            tarjeta = entrada.nextInt();
            entrada.nextLine();
        } catch (InputMismatchException e) {
            System.err.println("Solo puedes escribir numeros");
            entrada.nextLine();
            return;
        }

        System.out.print("Direccion: ");
        String direccion = entrada.nextLine();

        String contrasenaTemp = "temporal123";
        String contrasenaCifrada = SimpleBCrypt.hash(contrasenaTemp);

        Cliente cliente = new Cliente(nombre, apellidos, telefono, email, tarjeta, direccion, contrasenaCifrada);

        // USAR save() DE IRepositorio
        repoUsuarios.save(cliente);

        System.out.println("Cliente añadido.");
        System.out.println("Contraseña temporal: " + contrasenaTemp);
    }

    // ===================== AÑADIR TRABAJADOR - USA save() Y count() =====
    static void aniadirTrabajador() {
        System.out.println("\n========== AÑADIR TRABAJADOR ==========");

        System.out.print("Nombre: ");
        String nombre = entrada.nextLine();

        System.out.print("Apellidos: ");
        String apellidos = entrada.nextLine();

        System.out.print("Telefono: ");
        int telefono = 0;
        try {
            telefono = entrada.nextInt();
            entrada.nextLine();
        } catch (InputMismatchException e) {
            System.err.println("Solo puedes escribir numeros");
            entrada.nextLine();
            return;
        }

        System.out.print("Email: ");
        String email = entrada.nextLine();

        String puesto = validarOpcion("Puesto (" + String.join("/", PUESTOS) + "): ", PUESTOS);

        System.out.print("Salario: ");
        double salario = 0;
        try {
            salario = entrada.nextDouble();
            entrada.nextLine();
        } catch (InputMismatchException e) {
            System.err.println("Solo puedes escribir numeros");
            entrada.nextLine();
            return;
        }

        String turno = validarOpcion("Turno (" + String.join("/", TURNOS) + "): ", TURNOS);

        System.out.print("Contraseña: ");
        String contrasena = entrada.nextLine();

        String contrasenaCifrada = SimpleBCrypt.hash(contrasena);

        Trabajador trabajador = new Trabajador(nombre, apellidos, telefono, email, puesto, salario, turno, contrasenaCifrada);

        // USAR save() DE IRepositorio
        repoUsuarios.save(trabajador);

        System.out.println("Trabajador añadido.");
    }

    // ===================== ELIMINAR PEDIDO - USA findByIdOptional() Y deleteById() =====
    static void eliminarPedido() {
        System.out.println("\n========== ELIMINAR PEDIDO ==========");

        // USAR findAllToList() DE IRepositorioExtend
        List<Pedido> pedidos = repoPedido.findAllToList();

        if (pedidos.isEmpty()) {
            System.out.println("No hay pedidos para eliminar.");
            return;
        }

        System.out.println("Pedidos disponibles:");
        for (Pedido p : pedidos) {
            System.out.println("ID: " + p.getId() + " | " + p.getNumeroPedido() +
                    " | Usuario: " + (p.getUsuario() != null ? p.getUsuario().getNombre() : "Desconocido"));
        }

        System.out.print("ID del pedido a eliminar: ");
        int idPedido = 0;
        try {
            idPedido = entrada.nextInt();
            entrada.nextLine();
        } catch (InputMismatchException e) {
            System.err.println("Solo puedes escribir numeros");
            entrada.nextLine();
            return;
        }

        // USAR existsById() DE IRepositorio
        if (!repoPedido.existsById(idPedido)) {
            System.err.println("El pedido no existe.");
            return;
        }

        // USAR deleteById() DE IRepositorio
        repoPedido.deleteById(idPedido);
        System.out.println("Pedido eliminado correctamente.");
    }

    // ===================== ELIMINAR PIZZA - USA findByIdOptional() Y deleteById() =====
    static void eliminarPizza() {
        System.out.println("\n========== ELIMINAR PIZZA ==========");

        // USAR findAllToList() DE IRepositorioExtend
        List<Pizza> pizzas = repoPizza.findAllToList();

        if (pizzas.isEmpty()) {
            System.out.println("No hay pizzas para eliminar.");
            return;
        }

        System.out.println("Pizzas disponibles:");
        for (Pizza p : pizzas) {
            System.out.println("ID: " + p.getId() + " | " + p.getNombre() +
                    " | $" + p.getPrecio());
        }

        System.out.print("ID de la pizza a eliminar: ");
        int idPizza = 0;
        try {
            idPizza = entrada.nextInt();
            entrada.nextLine();
        } catch (InputMismatchException e) {
            System.err.println("Solo puedes escribir numeros");
            entrada.nextLine();
            return;
        }

        // USAR existsById() DE IRepositorio
        if (!repoPizza.existsById(idPizza)) {
            System.err.println("La pizza no existe.");
            return;
        }

        // USAR deleteById() DE IRepositorio
        repoPizza.deleteById(idPizza);
        System.out.println("Pizza eliminada correctamente.");
    }

    // ===================== MODIFICAR DATOS CLIENTE =====================
    static void modificarDatosCliente(Cliente cliente, RepositorioUsuario repo) throws SQLException {
        RepositorioUsuario repoUsuarios=new RepositorioUsuario(obtenerConexion());
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n=== MODIFICAR DATOS ===");
            System.out.println("1. Nombre: " + cliente.getNombre());
            System.out.println("2. Apellidos: " + cliente.getApellidos());
            System.out.println("3. Teléfono: " + cliente.getTelefono());
            System.out.println("4. Email: " + cliente.getEmail());
            System.out.println("5. Tarjeta crédito: " + cliente.getTarjetaCredito());
            System.out.println("6. Dirección de envío: " + cliente.getDireccion());
            System.out.println("7. Cambiar contraseña");
            System.out.println("0. Volver");
            System.out.print("Opción: ");

            opcion = sc.nextInt();
            sc.nextLine();

            Cliente nuevo = null;

            switch (opcion) {

                case 1 -> {
                    System.out.print("Nuevo nombre: ");
                    String nombre = sc.nextLine();
                    nuevo = new Cliente(
                            cliente.getId(), nombre, cliente.getApellidos(),
                            cliente.getTelefono(), cliente.getEmail(),
                            cliente.getTarjetaCredito(), cliente.getDireccion(),
                            cliente.getDescuento(), cliente.getContrasena()
                    );
                }

                case 2 -> {
                    System.out.print("Nuevos apellidos: ");
                    String ap = sc.nextLine();
                    nuevo = new Cliente(
                            cliente.getId(), cliente.getNombre(), ap,
                            cliente.getTelefono(), cliente.getEmail(),
                            cliente.getTarjetaCredito(), cliente.getDireccion(),
                            cliente.getDescuento(), cliente.getContrasena()
                    );
                }

                case 3 -> {
                    System.out.print("Nuevo teléfono: ");
                    int tel = sc.nextInt(); sc.nextLine();
                    nuevo = new Cliente(
                            cliente.getId(), cliente.getNombre(), cliente.getApellidos(),
                            tel, cliente.getEmail(),
                            cliente.getTarjetaCredito(), cliente.getDireccion(),
                            cliente.getDescuento(), cliente.getContrasena()
                    );
                }

                case 4 -> {
                    System.out.print("Nuevo email: ");
                    String email = sc.nextLine();
                    nuevo = new Cliente(
                            cliente.getId(), cliente.getNombre(), cliente.getApellidos(),
                            cliente.getTelefono(), email,
                            cliente.getTarjetaCredito(), cliente.getDireccion(),
                            cliente.getDescuento(), cliente.getContrasena()
                    );
                }

                case 5 -> {
                    System.out.print("Nueva tarjeta: ");
                    int tc = sc.nextInt(); sc.nextLine();
                    nuevo = new Cliente(
                            cliente.getId(), cliente.getNombre(), cliente.getApellidos(),
                            cliente.getTelefono(), cliente.getEmail(),
                            tc, cliente.getDireccion(),
                            cliente.getDescuento(), cliente.getContrasena()
                    );
                }

                case 6 -> {
                    System.out.print("Nueva dirección: ");
                    String dir = sc.nextLine();
                    nuevo = new Cliente(
                            cliente.getId(), cliente.getNombre(), cliente.getApellidos(),
                            cliente.getTelefono(), cliente.getEmail(),
                            cliente.getTarjetaCredito(), dir,
                            cliente.getDescuento(), cliente.getContrasena()
                    );
                }

                case 7 -> {
                    System.out.print("Nueva contraseña: ");
                    String pass = sc.nextLine();
                    String passHashed = SimpleBCrypt.hash(pass);

                    nuevo = new Cliente(
                            cliente.getId(), cliente.getNombre(), cliente.getApellidos(),
                            cliente.getTelefono(), cliente.getEmail(),
                            cliente.getTarjetaCredito(), cliente.getDireccion(),
                            cliente.getDescuento(), passHashed
                    );
                }

                case 0 -> {
                    return;
                }
            }

            if (nuevo != null) {
                // USAR save() DE IRepositorio
                repoUsuarios.actualizarUsuario(nuevo);
                cliente = nuevo;
                System.out.println("Datos actualizados.");
            }

        } while (true);
    }

    // ===================== MODIFICAR DATOS TRABAJADOR =====================
    static void modificarDatosTrabajador(Trabajador trabajador, RepositorioUsuario repo) throws SQLException {
        RepositorioUsuario repoUsuarios=new RepositorioUsuario(obtenerConexion());
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n=== MODIFICAR DATOS TRABAJADOR ===");
            System.out.println("1. Nombre: " + trabajador.getNombre());
            System.out.println("2. Apellidos: " + trabajador.getApellidos());
            System.out.println("3. Teléfono: " + trabajador.getTelefono());
            System.out.println("4. Email: " + trabajador.getEmail());
            System.out.println("5. Puesto: " + trabajador.getPuesto());
            System.out.println("6. Salario: " + trabajador.getSalario());
            System.out.println("7. Turno: " + trabajador.getTurno());
            System.out.println("8. Cambiar contraseña");
            System.out.println("0. Volver");
            System.out.print("Opción: ");

            opcion = sc.nextInt();
            sc.nextLine();

            Trabajador nuevo = null;

            switch (opcion) {

                case 1 -> {
                    System.out.print("Nuevo nombre: ");
                    String nombre = sc.nextLine();
                    nuevo = new Trabajador(
                            trabajador.getId(), nombre, trabajador.getApellidos(),
                            trabajador.getTelefono(), trabajador.getEmail(),
                            trabajador.getPuesto(), trabajador.getSalario(),
                            trabajador.getTurno(), trabajador.getContrasena()
                    );
                }

                case 2 -> {
                    System.out.print("Nuevos apellidos: ");
                    String ap = sc.nextLine();
                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), ap,
                            trabajador.getTelefono(), trabajador.getEmail(),
                            trabajador.getPuesto(), trabajador.getSalario(),
                            trabajador.getTurno(), trabajador.getContrasena()
                    );
                }

                case 3 -> {
                    System.out.print("Nuevo teléfono: ");
                    int tel = sc.nextInt(); sc.nextLine();
                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), trabajador.getApellidos(),
                            tel, trabajador.getEmail(),
                            trabajador.getPuesto(), trabajador.getSalario(),
                            trabajador.getTurno(), trabajador.getContrasena()
                    );
                }

                case 4 -> {
                    System.out.print("Nuevo email: ");
                    String email = sc.nextLine();
                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), trabajador.getApellidos(),
                            trabajador.getTelefono(), email,
                            trabajador.getPuesto(), trabajador.getSalario(),
                            trabajador.getTurno(), trabajador.getContrasena()
                    );
                }

                case 5 -> {
                    String nuevoPuesto = validarOpcion("Nuevo puesto (" + String.join("/", PUESTOS) + "): ", PUESTOS);
                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), trabajador.getApellidos(),
                            trabajador.getTelefono(), trabajador.getEmail(),
                            nuevoPuesto, trabajador.getSalario(),
                            trabajador.getTurno(), trabajador.getContrasena()
                    );
                }

                case 6 -> {
                    System.out.print("Nuevo salario: ");
                    double salario = sc.nextDouble(); sc.nextLine();
                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), trabajador.getApellidos(),
                            trabajador.getTelefono(), trabajador.getEmail(),
                            trabajador.getPuesto(), salario,
                            trabajador.getTurno(), trabajador.getContrasena()
                    );
                }

                case 7 -> {
                    String nuevoTurno = validarOpcion("Nuevo turno (" + String.join("/", TURNOS) + "): ", TURNOS);
                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), trabajador.getApellidos(),
                            trabajador.getTelefono(), trabajador.getEmail(),
                            trabajador.getPuesto(), trabajador.getSalario(),
                            nuevoTurno, trabajador.getContrasena()
                    );
                }

                case 8 -> {
                    System.out.print("Nueva contraseña: ");
                    String pass = sc.nextLine();
                    String passHashed = SimpleBCrypt.hash(pass);

                    nuevo = new Trabajador(
                            trabajador.getId(), trabajador.getNombre(), trabajador.getApellidos(),
                            trabajador.getTelefono(), trabajador.getEmail(),
                            trabajador.getPuesto(), trabajador.getSalario(),
                            trabajador.getTurno(), passHashed
                    );
                }

                case 0 -> {
                    return;
                }
            }

            if (nuevo != null) {
                // USAR save() DE IRepositorio
                repoUsuarios.actualizarUsuario(nuevo);
                trabajador = nuevo;
                System.out.println("Datos actualizados correctamente.");
            }

        } while (true);
    }
}

