package fr.perso.nfelix.app.ui.controllers;

/**
 * IFXWorkableArea
 *
 * @author N.FELIX
 */
public interface IFXWorkableArea {

  /**
   * enable or disbale UI
   *
   * @param enable true if enabled
   */
  void enableOrDisableUI(boolean enable);

  /**
   * enable UI
   */
  void enableUI();

  /**
   * disbale UI
   */
  void disableUI();

}
