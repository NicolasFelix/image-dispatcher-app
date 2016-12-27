package fr.perso.nfelix.app.ui.services;

import fr.perso.nfelix.app.exception.ConfigurationException;

/**
 * IPreValidateService
 *
 * @author N.FELIX
 */
public interface IPreValidateService {

  /**
   * call this to pre validate data/config before running the service
   *
   * @throws ConfigurationException thrown if any wrong configuration appears
   */
  void preValidate()
      throws ConfigurationException;
}
