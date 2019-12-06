package org.edmcouncil.spec.fibo.weasel.ontology.searcher.text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.provider.LabelProvider;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.ViewerSearcher;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.hint.HintItem;
import org.semanticweb.owlapi.model.IRI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class TextSearcher implements ViewerSearcher {

  @Autowired
  private TextSearcherDb db;
  @Autowired
  private LabelProvider labelExtractor;

  @Override
  public SearcherResult search() {

    return null;
  }

  public List<HintItem> getHints(String query, Integer maxHintCount) {
    List<HintItem> result = db.getHints(query, maxHintCount);
    result.forEach((hi) -> {
      String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(hi.getIri()));
      hi.setLabel(label);
    });
    Collections.sort(result, Comparator.comparing(HintItem::getRelevancy).reversed()
        .thenComparing(HintItem::getLabel).reversed());
    Collections.reverse(result);
    return result;
  }

}
