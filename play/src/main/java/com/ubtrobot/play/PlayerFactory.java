package com.ubtrobot.play;

public interface PlayerFactory {

    <O> Player createPlayer(Segment<O> segment);
}
