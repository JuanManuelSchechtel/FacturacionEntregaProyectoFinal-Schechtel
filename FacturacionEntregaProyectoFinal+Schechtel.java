import javax.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;
    private String nombre;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

import javax.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productoId;
    private String nombre;
    private double precio;
    private int stock;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date fecha;

    @ManyToOne
    private Cliente cliente;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL)
    private List<LineaComprobante> lineas;

    private double total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<LineaComprobante> getLineas() { return lineas; }
    public void setLineas(List<LineaComprobante> lineas) { this.lineas = lineas; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

import javax.persistence.*;

@Entity
public class LineaComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Comprobante comprobante;

    @ManyToOne
    private Producto producto;

    private int cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante comprobante) { this.comprobante = comprobante; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}

import java.util.List;

public class ComprobanteDTO {
    private ClienteDTO cliente;
    private List<LineaDTO> lineas;

    public ClienteDTO getCliente() { return cliente; }
    public void setCliente(ClienteDTO cliente) { this.cliente = cliente; }
    public List<LineaDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaDTO> lineas) { this.lineas = lineas; }
}

public class ClienteDTO {
    private Long clienteId;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}

public class LineaDTO {
    private int cantidad;
    private ProductoDTO producto;

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public ProductoDTO getProducto() { return producto; }
    public void setProducto(ProductoDTO producto) { this.product = producto; }
}

public class ProductoDTO {
    private Long productoId;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ComprobanteService {
    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Comprobante crearComprobante(ComprobanteDTO comprobanteDTO) throws Exception {
        if (!clienteRepository.existsById(comprobanteDTO.getCliente().getClienteId())) {
            throw new Exception("Cliente no existente");
        }

        double total = 0;
        for (LineaDTO linea : comprobanteDTO.getLineas()) {
            if (!productoRepository.existsById(linea.getProducto().getProductoId())) {
                throw new Exception("Producto no existente");
            }

            Producto producto = productoRepository.findById(linea.getProducto().getProductoId()).orElseThrow(() -> new Exception("Producto no encontrado"));
            if (linea.getCantidad() > producto.getStock()) {
                throw new Exception("Cantidad solicitada excede el stock");
            }

            total += producto.getPrecio() * linea.getCantidad();
            producto.setStock(producto.getStock() - linea.getCantidad());
            productoRepository.save(producto);
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setFecha(obtenerFecha());
        comprobante.setCliente(clienteRepository.findById(comprobanteDTO.getCliente().getClienteId()).orElse(null));
        comprobante.setTotal(total);
        return comprobanteRepository.save(comprobante);
    }

    private Date obtenerFecha() {
        return new Date();
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {
    @Autowired
    private ComprobanteService comprobanteService;

    @PostMapping
    public ResponseEntity<Comprobante> crearComprobante(@RequestBody ComprobanteDTO comprobanteDTO) {
        try {
            Comprobante comprobante = comprobanteService.crearComprobante(comprobanteDTO);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

CREATE TABLE cliente (
    cliente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE producto (
    producto_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
);

CREATE TABLE comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    cliente_id BIGINT,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id)
);

CREATE TABLE linea_comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id BIGINT,
    producto_id BIGINT,
    cantidad INT NOT NULL,
    FOREIGN KEY (comprobante_id) REFERENCES comprobante(id),
    FOREIGN KEY (producto_id) REFERENCES producto(producto_id)
);

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tu.paquete"))
                .paths(PathSelectors.any())
                .build();
    }
}

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e ) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
``` ```java
import javax.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;
    private String nombre;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

import javax.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productoId;
    private String nombre;
    private double precio;
    private int stock;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date fecha;

    @ManyToOne
    private Cliente cliente;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL)
    private List<LineaComprobante> lineas;

    private double total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<LineaComprobante> getLineas() { return lineas; }
    public void setLineas(List<LineaComprobante> lineas) { this.lineas = lineas; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

import javax.persistence.*;

@Entity
public class LineaComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Comprobante comprobante;

    @ManyToOne
    private Producto producto;

    private int cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante comprobante) { this.comprobante = comprobante; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}

import java.util.List;

public class ComprobanteDTO {
    private ClienteDTO cliente;
    private List<LineaDTO> lineas;

    public ClienteDTO getCliente() { return cliente; }
    public void setCliente(ClienteDTO cliente) { this.cliente = cliente; }
    public List<LineaDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaDTO> lineas) { this.lineas = lineas; }
}

public class ClienteDTO {
    private Long clienteId;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}

public class LineaDTO {
    private int cantidad;
    private ProductoDTO producto;

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public ProductoDTO getProducto() { return producto; }
    public void setProducto(ProductoDTO producto) { this.producto = producto; }
}

public class ProductoDTO {
    private Long productoId;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ComprobanteService {
    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Comprobante crearComprobante(ComprobanteDTO comprobanteDTO) throws Exception {
        if (!clienteRepository.existsById(comprobanteDTO.getCliente().getClienteId())) {
            throw new Exception("Cliente no existente");
        }

        double total = 0;
        for (LineaDTO linea : comprobanteDTO.getLineas()) {
            if (!productoRepository.existsById(linea.getProducto().getProductoId())) {
                throw new Exception("Producto no existente");
            }

            Producto producto = productoRepository.findById(linea.getProducto().getProductoId()).orElseThrow(() -> new Exception("Producto no encontrado"));
            if (linea.getCantidad() > producto.getStock()) {
                throw new Exception("Cantidad solicitada excede el stock");
            }

            total += producto.getPrecio() * linea.getCantidad();
            producto.setStock(producto.getStock() - linea.getCantidad());
            productoRepository.save(producto);
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setFecha(obtenerFecha());
        comprobante.setCliente(clienteRepository.findById(comprobanteDTO.getCliente().getClienteId()).orElse(null));
        comprobante.setTotal(total);
        return comprobanteRepository.save(comprobante);
    }

    private Date obtenerFecha() {
        return new Date();
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {
    @Autowired
    private ComprobanteService comprobanteService;

    @PostMapping
    public ResponseEntity<Comprobante> crearComprobante(@RequestBody ComprobanteDTO comprobanteDTO) {
        try {
            Comprobante comprobante = comprobanteService.crearComprobante(comprobanteDTO);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

CREATE TABLE cliente (
    cliente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE producto (
    producto_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
);

CREATE TABLE comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    cliente_id BIGINT,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id)
);

CREATE TABLE linea_comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id BIGINT,
    producto_id BIGINT,
    cantidad INT NOT NULL,
    FOREIGN KEY (comprobante_id) REFERENCES comprobante(id),
    FOREIGN KEY (producto_id) REFERENCES producto(producto_id)
);

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tu.paquete"))
                .paths(PathSelectors.any())
                .build();
    }
}

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
``` ```java
import javax.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;
    private String nombre;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

import javax.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productoId;
    private String nombre;
    private double precio;
    private int stock;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date fecha;

    @ManyToOne
    private Cliente cliente;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL)
    private List<LineaComprobante> lineas;

    private double total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<LineaComprobante> getLineas() { return lineas; }
    public void setLineas(List<LineaComprobante> lineas) { this.lineas = lineas; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

import javax.persistence.*;

@Entity
public class LineaComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Comprobante comprobante;

    @ManyToOne
    private Producto producto;

    private int cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante comprobante) { this.comprobante = comprobante; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}

import java.util.List;

public class ComprobanteDTO {
    private ClienteDTO cliente;
    private List<LineaDTO> lineas;

    public ClienteDTO getCliente() { return cliente; }
    public void setCliente(ClienteDTO cliente) { this.cliente = cliente; }
    public List<LineaDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaDTO> lineas) { this.lineas = lineas; }
}

public class ClienteDTO {
    private Long clienteId;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}

public class LineaDTO {
    private int cantidad;
    private ProductoDTO producto;

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public ProductoDTO getProducto() { return producto; }
    public void setProducto(ProductoDTO producto) { this.producto = producto; }
}

public class ProductoDTO {
    private Long productoId;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}

import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ComprobanteService {
    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Comprobante crearComprobante(ComprobanteDTO comprobanteDTO) throws Exception {
        if (!clienteRepository.existsById(comprobanteDTO.getCliente().getClienteId())) {
            throw new Exception("Cliente no existente");
        }

        double total = 0;
        for (LineaDTO linea : comprobanteDTO.getLineas()) {
            if (!productoRepository.existsById(linea.getProducto().getProductoId())) {
                throw new Exception("Producto no existente");
            }

            Producto producto = productoRepository.findById(linea.getProducto().getProductoId()).orElseThrow(() -> new Exception("Producto no encontrado"));
            if (linea.getCantidad() > producto.getStock()) {
                throw new Exception("Cantidad solicitada excede el stock");
            }

            total += producto.getPrecio() * linea.getCantidad();
            producto.setStock(producto.getStock() - linea.getCantidad());
            productoRepository.save(producto);
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setFecha(obtenerFecha());
        comprobante.setCliente(clienteRepository.findById(comprobanteDTO.getCliente().getClienteId()).orElse(null));
        comprobante.setTotal(total);
        return comprobanteRepository.save(comprobante);
    }

    private Date obtenerFecha() {
        return new Date();
    }
}

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {
    @Autowired
    private ComprobanteService comprobanteService;

    @PostMapping
    public ResponseEntity<Comprobante> crearComprobante(@RequestBody ComprobanteDTO comprobanteDTO) {
        try {
            Comprobante comprobante = comprobanteService.crearComprobante(comprobanteDTO);
            return ResponseEntity.ok(comprobante);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}

CREATE TABLE cliente (
    cliente_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

CREATE TABLE producto (
    producto_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL
);

CREATE TABLE comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    cliente_id BIGINT,
    total DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (cliente_id) REFERENCES cliente(cliente_id)
);

CREATE TABLE linea_comprobante (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comprobante_id BIGINT,
    producto_id BIGINT,
    cantidad INT NOT NULL,
    FOREIGN KEY (comprobante_id) REFERENCES comprobante(id),
    FOREIGN KEY (producto_id) REFERENCES producto(producto_id)
);

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tu.paquete"))
                .paths(PathSelectors.any())
                .build();
    }
}

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
``` ```java
import javax.persistence.*;

@Entity
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clienteId;
    private String nombre;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}

import javax.persistence.*;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productoId;
    private String nombre;
    private double precio;
    private int stock;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date fecha;

    @ManyToOne
    private Cliente cliente;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL)
    private List<LineaComprobante> lineas;

    private double total;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public List<LineaComprobante> getLineas() { return lineas; }
    public void setLineas(List<LineaComprobante> lineas) { this.lineas = lineas; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}

import javax.persistence.*;

@Entity
public class LineaComprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Comprobante comprobante;

    @ManyToOne
    private Producto producto;

    private int cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante comprobante) { this.comprobante = comprobante; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}

import java.util.List;

public class ComprobanteDTO {
    private ClienteDTO cliente;
    private List<LineaDTO> lineas;

    public ClienteDTO getCliente() { return cliente; }
    public void setCliente(ClienteDTO cliente) { this.cliente = cliente; }
    public List<LineaDTO> getLineas() { return lineas; }
    public void setLineas(List<LineaDTO> lineas) { this.lineas = lineas; }
}

public class ClienteDTO {
    private Long clienteId;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}

public class LineaDTO {
    private int cantidad;
    private ProductoDTO producto;

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public ProductoDTO getProducto() { return producto; }
    public void setProducto(ProductoDTO producto) { this.producto = producto; }
}