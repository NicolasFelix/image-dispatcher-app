package fr.perso.nfelix.app.ui.controllers.fragments;

import javafx.scene.control.Tab;
import lombok.Getter;
import lombok.Setter;

/**
 * AbstractTabFragment class
 *
 * @author N.FELIX
 */
public abstract class AbstractTabFragment extends AbstractFxFragment {

  @Setter
  @Getter
  private Tab parentTab;
}
