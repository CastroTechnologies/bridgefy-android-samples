package com.bridgefy.samples.tic_tac_toe.entities;

import com.bridgefy.samples.tic_tac_toe.BridgefyListener;
import com.bridgefy.sdk.client.Message;
import com.google.gson.Gson;

import java.util.HashMap;

/**
 * @author dekaru on 5/9/17.
 */

public class Move {

    String mid;
    Participants participants;
    int[][] board = new int[3][3];
    int seq;
    String winner;


    public Move(String mid, int seq, int[][] board) {
        this.mid = mid;
        this.seq = seq;
        this.board = board;
    }

    public HashMap<String, Object> toHashMap() {
        return new Event<>(
                Event.EventType.MOVE_EVENT,
                this).toHashMap();
    }

    public static Move create(Message message) {
        return new Gson().fromJson(
                new Gson().toJson(message.getContent().get("content")),
                Move.class);
    }

    public static Move create(String json) {
        return new Gson().fromJson(json, Move.class);
    }

    /**
     * @return The UUID String of the user playing against the current user. Null if myMatch() is false.
     */
    public String getOtherUuid() {
        if (myMatch())
            return BridgefyListener.getUuid().equals(participants.getO()) ?
                    participants.getX() : participants.getO();
        else
            return null;
    }

    /**
     * @return True if this Move belongs to a Match that belongs to the current user
     */
    public boolean myMatch() {
        return (BridgefyListener.getUuid().equals(participants.getO()) ||
                BridgefyListener.getUuid().equals(participants.getX()));
    }

    public void setParticipants(Participants participants) {
        this.participants = participants;
    }

    public Participants getParticipants() {
        return participants;
    }

    public int getSequence() {
        return seq;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getWinner() {
        return winner;
    }

    public String getMatchId() {
        return mid;
    }

    public int[][] getBoard() {
        return board;
    }


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
