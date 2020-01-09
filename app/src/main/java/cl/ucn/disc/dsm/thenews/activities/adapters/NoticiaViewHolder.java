package cl.ucn.disc.dsm.thenews.activities.adapters;

import androidx.recyclerview.widget.RecyclerView;
import cl.ucn.disc.dsm.thenews.databinding.RowNoticiaBinding;
import cl.ucn.disc.dsm.thenews.model.Noticia;

public final class NoticiaViewHolder extends RecyclerView.ViewHolder {

  /**
   * The Bindings
   */
  private final RowNoticiaBinding binding;

  /**
   * The Constructor.
   *
   * @param rowNoticiaBinding to use.
   */
  public NoticiaViewHolder(RowNoticiaBinding rowNoticiaBinding) {
    super(rowNoticiaBinding.getRoot());
    this.binding = rowNoticiaBinding;
  }

  /**
   * Bind the Noticia to the ViewHolder.
   *
   * @param noticia to bind.
   */
  public void bind(final Noticia noticia) {

    this.binding.tvTitulo.setText(noticia.getTitulo());
    this.binding.tvResumen.setText(noticia.getResumen());
    this.binding.tvAutor.setText(noticia.getAutor());
    this.binding.tvFuente.setText(noticia.getFuente());

    // FIXME: The format of the date.
    this.binding.tvFecha.setText(noticia.getFecha().toString());

  }

}