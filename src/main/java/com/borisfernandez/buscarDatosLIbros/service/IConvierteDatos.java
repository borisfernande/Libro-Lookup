package com.borisfernandez.buscarDatosLIbros.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
