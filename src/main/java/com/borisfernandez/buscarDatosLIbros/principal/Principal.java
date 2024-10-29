package com.borisfernandez.buscarDatosLIbros.principal;

import com.borisfernandez.buscarDatosLIbros.model.Datos;
import com.borisfernandez.buscarDatosLIbros.model.DatosLibros;
import com.borisfernandez.buscarDatosLIbros.service.ConsumoAPI;
import com.borisfernandez.buscarDatosLIbros.service.ConvertirDatos;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.Optional;
import java.util.Scanner;
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
        System.out.println("TOP 10 LIBROS MAS DESCARGADOS");
        datos.libros().stream()
                .sorted(Comparator.comparing(DatosLibros::totalDescargas).reversed())
                .limit(10)
                .map(e -> e.titulo().toUpperCase())
                .forEach(System.out::println);

        System.out.println("***********************************");
        //Busqueda de libros por nombre
        System.out.println("BUSCAR LIBROS POR NOMBRE");
        System.out.println("Ingresa el nombre del libro que deseas buscar");
        String nombreLibro = scanner.nextLine();
        String nuevaURL = URL_BASE + nombreLibro.replace(" ", "+");
        json = consumoAPI.obtenerDatos(nuevaURL);
        var datosBusqueda = conversor.obtenerDatos(json, Datos.class);


        Optional<DatosLibros> libroBuscado = datosBusqueda.libros().stream()
                .filter(e -> e.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .findFirst();
        if (libroBuscado.isPresent()){
            System.out.println("Libro encontrado");
            System.out.println(libroBuscado.get());
        }else {
            System.out.println("Libro no encontrado");
        }

        //Trabajando con estadisticas
        System.out.println("Estadistica con todos los libros");
        DoubleSummaryStatistics est = datos.libros().stream()
                .filter(e -> e.totalDescargas() > 0)
                .collect(Collectors.summarizingDouble(DatosLibros::totalDescargas));
        System.out.println("Cantidad de registros evaluados para calcular las estadisticas: "+ est.getCount());
        System.out.println("Cantidad media de descargas: " + est.getAverage());
        System.out.println("Cantidad maxima de descargas: " + est.getMax());
        System.out.println("Cantidad minima de descargas: " + est.getMin());
    }
}
