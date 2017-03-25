package com.art.alligator;

import com.art.alligator.animations.TransitionAnimation;

/**
 * Date: 24.02.2017
 * Time: 19:00
 *
 * @author Artur Artikov
 */

public interface TransitionAnimationProvider {
	TransitionAnimation getAnimation(TransitionType transitionType, Class<? extends Screen> screenClassFrom, Class<? extends Screen> screenClassTo, boolean isActivity, AnimationData animationData);
}
