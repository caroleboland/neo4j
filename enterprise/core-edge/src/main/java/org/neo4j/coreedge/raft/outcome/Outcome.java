/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.coreedge.raft.outcome;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.coreedge.raft.RaftMessages;
import org.neo4j.coreedge.raft.roles.Role;
import org.neo4j.coreedge.raft.state.FollowerStates;
import org.neo4j.coreedge.raft.state.ReadableRaftState;

public class Outcome<MEMBER> implements Serializable
{
    /* Common */
    public Role newRole;

    public long term;
    public MEMBER leader;

    public long leaderCommit;

    public ArrayList<LogCommand> logCommands = new ArrayList<>();
    public ArrayList<RaftMessages.Directed<MEMBER>> outgoingMessages = new ArrayList<>();

    /* Follower */
    public MEMBER votedFor;
    public boolean renewElectionTimeout;

    /* Candidate */
    public HashSet<MEMBER> votesForMe;
    public long lastLogIndexBeforeWeBecameLeader;

    /* Leader */
    public FollowerStates<MEMBER> followerStates;
    public ArrayList<ShipCommand> shipCommands = new ArrayList<>();

    public Outcome( Role currentRole, ReadableRaftState<MEMBER> ctx )
    {
        defaults( currentRole, ctx );
    }

    public Outcome( Role newRole, long term, MEMBER leader, long leaderCommit, MEMBER votedFor,
            Set<MEMBER> votesForMe, long lastLogIndexBeforeWeBecameLeader,
            FollowerStates<MEMBER> followerStates, boolean renewElectionTimeout,
            Collection<LogCommand> logCommands, Collection<RaftMessages.Directed<MEMBER>> outgoingMessages,
            Collection<ShipCommand> shipCommands )
    {
        this.newRole = newRole;
        this.term = term;
        this.leader = leader;
        this.leaderCommit = leaderCommit;
        this.votedFor = votedFor;
        this.votesForMe = new HashSet<>( votesForMe );
        this.lastLogIndexBeforeWeBecameLeader = lastLogIndexBeforeWeBecameLeader;
        this.followerStates = followerStates;
        this.renewElectionTimeout = renewElectionTimeout;

        this.logCommands.addAll( logCommands );
        this.outgoingMessages.addAll( outgoingMessages );
        this.shipCommands.addAll( shipCommands );
    }

    private void defaults( Role currentRole, ReadableRaftState<MEMBER> ctx )
    {
        newRole = currentRole;

        term = ctx.term();
        leader = ctx.leader();

        leaderCommit = ctx.leaderCommit();

        votedFor = ctx.votedFor();
        renewElectionTimeout = false;

        votesForMe = (currentRole == Role.CANDIDATE) ? new HashSet<>( ctx.votesForMe() ) : new HashSet<>();

        lastLogIndexBeforeWeBecameLeader = (currentRole == Role.LEADER) ? ctx.lastLogIndexBeforeWeBecameLeader() : -1;
        followerStates = (currentRole == Role.LEADER) ? ctx.followerStates() : new FollowerStates<>();
    }

    public void setNextRole( Role nextRole )
    {
        this.newRole = nextRole;
    }

    public void setNextTerm( long nextTerm )
    {
        this.term = nextTerm;
    }

    public void setLeader( MEMBER leader )
    {
        this.leader = leader;
    }

    public void setLeaderCommit( long leaderCommit )
    {
        this.leaderCommit = leaderCommit;
    }

    public void addLogCommand( LogCommand logCommand )
    {
        this.logCommands.add( logCommand );
    }

    public void addOutgoingMessage( RaftMessages.Directed<MEMBER> message )
    {
        this.outgoingMessages.add( message );
    }

    public void setVotedFor( MEMBER votedFor )
    {
        this.votedFor = votedFor;
    }

    public void renewElectionTimeout()
    {
        this.renewElectionTimeout = true;
    }

    public void addVoteForMe( MEMBER voteFrom )
    {
        this.votesForMe.add( voteFrom );
    }

    @Override
    public String toString()
    {
        return "Outcome{" +
               "nextRole=" + newRole +
               ", newTerm=" + term +
               ", leader=" + leader +
               ", leaderCommit=" + leaderCommit +
               ", logCommands=" + logCommands +
               ", shipCommands=" + shipCommands +
               ", votedFor=" + votedFor +
               ", updatedVotesForMe=" + votesForMe +
               ", lastLogIndexBeforeWeBecameLeader=" + lastLogIndexBeforeWeBecameLeader +
               ", updatedFollowerStates=" + followerStates +
               ", renewElectionTimeout=" + renewElectionTimeout +
               ", outgoingMessages=" + outgoingMessages +
               '}';
    }

    public void setLastLogIndexBeforeWeBecameLeader( long lastLogIndexBeforeWeBecameLeader )
    {
        this.lastLogIndexBeforeWeBecameLeader = lastLogIndexBeforeWeBecameLeader;
    }

    public void addShipCommand( ShipCommand shipCommand )
    {
        shipCommands.add( shipCommand );
    }
}
