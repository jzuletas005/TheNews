package cl.ucn.disc.dsm.thenews.services.newsapi;



import cl.ucn.disc.dsm.thenews.model.Noticia;
import cl.ucn.disc.dsm.thenews.services.NoticiaService;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import net.openhft.hashing.LongHashFunction;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeParseException;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsApiNoticiaService {


  private static final Logger log = LoggerFactory.getLogger(NewsApiNoticiaService.class);
  /**
   * Test {@link NoticiaService#getNoticias(int)} with NewsAPI.org
   */
  @Test
  public void testGetNoticiasNewsApi() {

    final int size = 20;


    log.debug("Testing the NewsApiNoticiaService, requesting {} News.", size);

    // The noticia service
    final NoticiaService noticiaService = (NoticiaService) new NewsApiNoticiaService();

    // The List of Noticia.
    final List<Noticia> noticias = noticiaService.getNoticias(size);

    Assertions.assertNotNull(noticias);
    Assertions.assertEquals(String.valueOf(noticias.size()), size, "Error de tamanio");

    for (final Noticia noticia : noticias) {
      log.debug("Noticia: {}.", noticia);
    }

    log.debug("Done.");

  }
  /**
   * The NewsAPI
   */
  private final NewsApi newsApi;

  /**
   * The Constructor.
   */
  public NewsApiNoticiaService() {

    // https://futurestud.io/tutorials/retrofit-getting-started-and-android-client
    this.newsApi = new Retrofit.Builder()
        // The main URL
        .baseUrl(NewsApi.BASE_URL)
        // JSON to POJO
        .addConverterFactory(GsonConverterFactory.create())
        // Validate the interface
        .validateEagerly(true)
        // Build the Retrofit ..
        .build()
        // .. get the NewsApi.
        .create(NewsApi.class);
  }
  /**
   * Get the Noticias from the Call.
   *
   * @param theCall to use.
   * @return the {@link List} of {@link Noticia}.
   */
  private static List<Object> getNoticiasFromCall(final Call<NewsApiResult> theCall) {

    try {

      // Get the result from the call
      final Response<NewsApiResult> response = theCall.execute();

      // UnSuccessful !
      if (!response.isSuccessful()) {

        // Error!
        throw new NewsAPIException(
            "Can't get the NewsResult, code: " + response.code(),
            new HttpException(response)
        );

      }

      final NewsApiResult theResult = response.body();

      // No body
      if (theResult == null) {
        throw new NewsAPIException("NewsResult was null");
      }

      // No articles
      if (theResult.articles == null) {
        throw new NewsAPIException("Articles in NewsResult was null");
      }

      // Article to Noticia (transformer)
      return theResult.articles.stream()
          .map(Transformer::transform)
          .collect(Collectors.toList());

    } catch (final IOException ex) {
      throw new NewsAPIException("Can't get the NewsResult", ex);
    }

  }

  /**
   * Article to Noticia.
   *
   * @param article to transform
   * @return the Noticia.
   */
  public static Noticia transform(final Article article) {

    // Nullity
    if (article == null) {
      throw new NewsApiNoticiaService.NewsAPIException("Article was null");
    }

    // The host
    final String host = getHost(article.url);

    // Si el articulo es null ..

    if (article.title == null) {

      log.warn("Article without title: {}", toString(article));

      // .. y el contenido es null, lanzar exception!
      if (article.description == null) {
        throw new NewsApiNoticiaService.NewsAPIException("Article without title and description");
      }

      // FIXME: Cambiar el titulo por alguna informacion disponible
      article.title = "No Title*";
    }

    // FIXED: En caso de no haber una fuente.
    if (article.source == null) {
      article.source = new Source();

      if (host != null) {
        article.source.name = host;
      } else {
        article.source.name = "No Source*";
        log.warn("Article without source: {}", toString(article));
      }
    }

    // FIXED: Si el articulo no tiene author
    if (article.author == null) {

      if (host != null) {
        article.author = host;
      } else {
        article.author = "No Author*";
        log.warn("Article without author: {}", toString(article));
      }
    }

    // The date.
    final ZonedDateTime publishedAt = parseZonedDateTime(article.publishedAt)
        .withZoneSameInstant(Noticia.ZONE_ID);

    // The unique id (computed from hash)
    final Long theId = LongHashFunction.xx()
        .hashChars(article.title + article.source.name);

    // The Noticia.
    return new Noticia(
        theId,
        article.title,
        article.source.name,
        article.author,
        article.url,
        article.urlToImage,
        article.description,
        article.content,
        publishedAt
    );

  }

  /**
   * Convierte una fecha de {@link String} a una {@link ZonedDateTime}.
   *
   * @param fecha to parse.
   * @return the fecha.
   * @throws cl.ucn.disc.dsm.thenews.services.newsapi.NewsApiNoticiaService.NewsAPIException en caso de no lograr
   *                                                                                         convertir la fecha.
   */
  private static ZonedDateTime parseZonedDateTime(final String fecha) {

    // Na' que hacer si la fecha no existe
    if (fecha == null) {
      throw new NewsApiNoticiaService.NewsAPIException("Can't parse null fecha");
    }

    try {
      // Tratar de convertir la fecha ..
      return ZonedDateTime.parse(fecha);
    } catch (DateTimeParseException ex) {

      // Mensaje de debug

      log.error("Can't parse date: ->{}<-. Error: ", fecha, ex);

      // Anido la DateTimeParseException en una NoticiaTransformerException.
      throw new NewsApiNoticiaService.NewsAPIException("Can't parse date: " + fecha, ex);
    }
  }

  /**
   * Get the host part of one url.
   *
   * @param url to use.
   * @return the host part (without the www)
   */
  private static String getHost(final String url) {

    try {

      final URI uri = new URI(url);
      final String hostname = uri.getHost();

      // to provide faultproof result, check if not null then return only hostname, without www.
      if (hostname != null) {
        return hostname.startsWith("www.") ? hostname.substring(4) : hostname;
      }

      return null;

    } catch (final URISyntaxException | NullPointerException ex) {
      return null;
    }
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
   * The Exception.
   */
  public static final class NewsAPIException extends RuntimeException {

    public NewsAPIException(final String message) {
      super(message);
    }

    public NewsAPIException(final String message, final Throwable cause) {
      super(message, cause);
    }

  }

  /**
   * Get the Noticias from the Call.
   *
   * @param pageSize how many.
   * @return the {@link List} of {@link Noticia}.
   */
  //@Override
  public List<Object> getNoticias(int pageSize) {

    // the Call
    final Call<NewsApiResult> theCall = this.newsApi.getEverything(pageSize);

    // Process the Call.
    return getNoticiasFromCall(theCall);
  }
}
