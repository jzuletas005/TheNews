package cl.ucn.disc.dsm.thenews;

import cl.ucn.disc.dsm.thenews.model.Noticia;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Transformer<T> {

  /**
   * The Logger
   */
  private static final Logger log = LoggerFactory.getLogger(Transformer.class);

  /**
   * El transformador de Noticias.
   */
  private final NoticiaTrasformer<T> noticiaTrasformer;

  /**
   * The Constructor.
   *
   * @param noticiaTrasformer a usar para la conversion.
   */
  public Transformer(final NoticiaTrasformer<T> noticiaTrasformer) {
    Objects.requireNonNull(noticiaTrasformer, "Se requiere un transformador de noticias");
    this.noticiaTrasformer = noticiaTrasformer;
  }

  /**
   * Transforma en String un objeto t mostrando sus atributos.
   *
   * @param t   to convert.
   * @param <T> type of t.
   * @return the object in string format.
   */
  public static <T> String toString(final T t) {
    return ReflectionToStringBuilder.toString(t, ToStringStyle.MULTI_LINE_STYLE);
  }

  /**
   * Transforma una {@link Collection} de T en un {@link List} de {@link Noticia}.
   *
   * @param collection fuente de T.
   * @return the List of Noticia.
   */
  public List<Noticia> transform(final Collection<T> collection) {

    // No se permiten nulls
    Objects.requireNonNull(collection, "No se permite una Collection null");

    // MICROPT: Tamanio de la coleccion igual a la lista
    final List<Noticia> noticias = new ArrayList<>(collection.size());

    for (final T t : collection) {

      try {
        // Transformo y agrego a la lista de noticias el t.
        noticias.add(this.noticiaTrasformer.transform(t));
      } catch (NoticiaTransformerException ex) {
        // .. si se produce una exception, despliego un warning y se omite.
        log.warn("Article skipped: {}", ex.getMessage());
      }

    }

    return noticias;

  }


  /**
   * Responsable de transformar una T en una {@link Noticia}.
   *
   * @param <T> a usar como base.
   */
  public interface NoticiaTrasformer<T> {

    /**
     * @param t a transformar.
     * @return the Noticia a partir de t.
     */
    Noticia transform(T t);

  }

  /**
   * La exception en caso de algun error en la transformacion.
   */
  public static final class NoticiaTransformerException extends RuntimeException {

    /**
     * @see RuntimeException
     */
    public NoticiaTransformerException(final String message) {
      super(message);
    }

    /**
     * @see RuntimeException
     */
    public NoticiaTransformerException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

}