package com.borisfernandez.buscarDatosLIbros.principal;

import com.borisfernandez.buscarDatosLIbros.model.Datos;
import com.borisfernandez.buscarDatosLIbros.model.DatosLibros;
import com.borisfernandez.buscarDatosLIbros.service.ConsumoAPI;
import com.borisfernandez.buscarDatosLIbros.service.ConvertirDatos;
import com.borisfernandez.buscarDatosLIbros.model.DatosAutores;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner scanner = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvertirDatos conversor = new ConvertirDatos();
    private static String URL_BASE = "https://gutendex.com/books/?search=";

    public void muestraResultados(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        System.out.println(json);
        var datos = conversor.obtenerDatos(json, Datos.class);

        //Top 10 libros mas descargados
        System.out.println("************************************");
        System.out.println("TOP 10 LIBROS MAS DESCARGADOS");
        datos.libros().stream()
                .sorted(Comparator.comparing(DatosLibros::totalDescargas).reversed())
                .limit(10)
                .map(e -> e.titulo().toUpperCase())
                .forEach(System.out::println);

        //Busqueda de libros por nombre
        System.out.println("***********************************");
        System.out.println("BUSCAR LIBROS POR NOMBRE");
        System.out.println("Ingresa el nombre del libro que deseas buscar");
        String nombreLibros = scanner.nextLine();
        String nuevaURL = URL_BASE + nombreLibros.replace(" ", "+");
        json = consumoAPI.obtenerDatos(nuevaURL);
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);
        
        Optional<DatosLibros> libroBuscado = datosBusqueda.libros().stream()
                .filter(e -> e.titulo().toUpperCase().contains(nombreLibros.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()){
            System.out.println("Libro encontrado");
            System.out.println(libroBuscado.get());
        }else {
            System.out.println("Libro no encontrado");
        }

        //Trabajando con estadisticas
        System.out.println("************************************");
        System.out.println("Estadistica con todos los libros");
        DoubleSummaryStatistics est = datos.libros().stream()
                .filter(e -> e.totalDescargas() > 0)
                .collect(Collectors.summarizingDouble(DatosLibros::totalDescargas));
        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: "+ est.getCount());
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad maxima de descargas: " + est.getMax());
        System.out.println("Cantidad minima de descargas: " + est.getMin());

        //Busqueda de autores por edad de muerte y nombre de libro
        System.out.println("************************************");
        System.out.println("Indica a que edad fallecio el autor");
        int edadAutor = scanner.nextInt();
        scanner.nextLine();

        List<DatosAutores> lista = datos.libros().stream()
                .flatMap(e -> e.autores().stream())
                .filter(e -> e.añoNacimiento() > 0 && e.añoFallecimiento() > 0)
                .filter(e -> e.añoFallecimiento() - e.añoNacimiento() == edadAutor)
                .collect(Collectors.toList());
        Iterator comprobar = lista.iterator();
        if (comprobar.hasNext()) {
            System.out.println("Autores que fallecieron a la edad de " + edadAutor + ":");
            lista.forEach(e -> System.out.println("Nombre: " + e.nombre() + ", Año de fallecimiento: " + e.añoFallecimiento()));

            System.out.println("Ahora indica el nombre del autor");
            String nombreAutor = scanner.nextLine();
            Optional<DatosLibros> datosLibrosPorAutor = datos.libros().stream()
                    .filter(e -> e.autores().stream().anyMatch(a -> a.nombre().toUpperCase().contains(nombreAutor.toUpperCase())))
                    .findFirst();
            if (datosLibrosPorAutor.isPresent()) {
                System.out.println("Se encontro el autor para ver sus libros:");
                System.out.println("Los datos son: ID: " + datosLibrosPorAutor.get().id() +
                        " Titulo: " + datosLibrosPorAutor.get().titulo() +
                        " Total Descargas: " + datosLibrosPorAutor.get().totalDescargas());
            } else {
                System.out.println("No se encontro autor");
            }
        }else {
            System.out.println("No se encontro ningun autor que murio a la eda " + edadAutor);
        }
    }
}
