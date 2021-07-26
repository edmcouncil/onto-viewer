package org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.PairImpl;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;

/**
 * Create by Michał Daniel (michal.daniel@makolab.com)
 */
@Deprecated
public class PairItem extends PairImpl<String, String> implements ConfigItem {

  private ConfigItemType type;

  @Override
  public ConfigItemType getType() {
    return this.type;
  }

}
