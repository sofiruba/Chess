package com.chess.domain.rule;

import com.chess.domain.model.*;
import com.chess.domain.port.*;
import com.chess.domain.command.*;
import com.chess.domain.movement.*;
import com.chess.domain.rule.*;
import com.chess.domain.draw.*;
import com.chess.domain.state.*;
import com.chess.domain.game.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses codes made of origin, destination and an optional promotion letter. The letters it accepts are the
 * notation codes of the promotion types it is built with, so it never needs to know the concrete pieces.
 */
public final class MoveCodeParser implements IMoveCodeParser {

    private static final Pattern CODE = Pattern.compile("([a-z])([1-9][0-9]?)([a-z])([1-9][0-9]?)([a-z])?");
    private static final int FROM_FILE = 1;
    private static final int FROM_RANK = 2;
    private static final int TO_FILE = 3;
    private static final int TO_RANK = 4;
    private static final int PROMOTION = 5;

    private final Map<String, PieceType> promotionTypesByCode;

    public MoveCodeParser(List<PieceType> promotionTypes) {
        Objects.requireNonNull(promotionTypes, "promotionTypes must not be null");
        this.promotionTypesByCode = indexByCode(promotionTypes);
    }

    @Override
    public MoveRequest parse(String moveCode) {
        Objects.requireNonNull(moveCode, "moveCode must not be null");
        Matcher matcher = CODE.matcher(moveCode);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid move code: '" + moveCode + "'");
        }
        Position from = position(matcher.group(FROM_FILE), matcher.group(FROM_RANK));
        Position to = position(matcher.group(TO_FILE), matcher.group(TO_RANK));
        return new MoveRequest(from, to, promotionType(moveCode, matcher.group(PROMOTION)));
    }

    private Position position(String file, String rank) {
        return new Position(file.charAt(0) - 'a', Integer.parseInt(rank) - 1);
    }

    private PieceType promotionType(String moveCode, String letter) {
        if (letter == null) {
            return null;
        }
        PieceType type = promotionTypesByCode.get(letter);
        if (type == null) {
            throw new IllegalArgumentException("Unknown promotion piece '" + letter + "' in move code: " + moveCode);
        }
        return type;
    }

    private static Map<String, PieceType> indexByCode(List<PieceType> types) {
        Map<String, PieceType> index = new HashMap<>();
        for (PieceType type : types) {
            if (index.put(type.notationCode(), type) != null) {
                throw new IllegalArgumentException("Two promotion types share the code " + type.notationCode());
            }
        }
        return Map.copyOf(index);
    }
}