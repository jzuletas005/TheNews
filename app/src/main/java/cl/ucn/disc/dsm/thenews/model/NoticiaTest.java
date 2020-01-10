package cl.ucn.disc.dsm.thenews.model;

import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;


public class NoticiaTest {

  /**
   * The Logger.
   */
  private static final Logger log = LoggerFactory.getLogger(NoticiaTest.class);


  /**
   * The Test of Constructor.
   */
  @Test
  public void testConstructor() {

    log.debug("Testing the Constructor ..");

    // The values
    final Long id = 1L;
    final String titulo = "The Titulo";
    final String fuente = "The Fuente";
    final String autor = "The Author";
    final String url = "The URL";
    final String urlFoto = "The URL of Foto";
    final String resumen = "The Resumen";
    final String contenido = "The Contenido";
    final ZonedDateTime fecha = ZonedDateTime.now(Noticia.ZONE_ID);

    // The Constructor
    final Noticia noticia = new Noticia(id, titulo, fuente, autor, url, urlFoto, resumen, contenido, fecha);

    // Testing
    Assert.assertEquals(id, noticia.getId());
    Assert.assertEquals(titulo, noticia.getTitulo());
    Assert.assertEquals(fuente, noticia.getFuente());
    Assert.assertEquals(autor, noticia.getAutor());
    Assert.assertEquals(url, noticia.getUrl());
    Assert.assertEquals(urlFoto, noticia.getUrlFoto());
    Assert.assertEquals(resumen, noticia.getResumen());
    Assert.assertEquals(contenido, noticia.getContenido());
    Assert.assertEquals(fecha, noticia.getFecha());

    log.debug("Done.");

  }

}