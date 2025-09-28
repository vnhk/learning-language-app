package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.view.AbstractPageView;

public abstract class AbstractLearningAppHomeView extends AbstractPageView {

    public AbstractLearningAppHomeView(MenuNavigationComponent menuNavigationLayout) {
        add(menuNavigationLayout);
    }

}
