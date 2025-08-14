// src/services/groupService.ts

import authService from './authService';

interface CreateGroupRequest {
  name: string;
  minContribution: number;
  maxMembers: number;
  description?: string;
  profileImage?: string;
  visibility: string;
  contributionFrequency: string;
  payoutFrequency: string;
  members?: string[];
  contributionDate?: string;
  payoutDate?: string;
}

interface UpdateGroupRequest {
  name?: string;
  description?: string;
  profileImage?: string;
  minContribution?: number;
  maxMembers?: number;
  visibility?: string;
  contributionFrequency?: string;
  payoutFrequency?: string;
  contributionDate?: string;
  payoutDate?: string;
}

interface Group {
  id: string;
  groupId: string;
  name: string;
  description: string;
  profileImage: string;
  visibility: string;
  minContribution: number;
  maxMembers: number;
  contributionFrequency: string;
  payoutFrequency: string;
  balance: number;
  createdAt: string;
  members: Member[];
  payoutOrder?: string[];
}

interface Member {
  userId: string;
  username: string;
  role: string;
  joinedAt: string;
}

interface JoinRequest {
  requestId: string;
  userId: string;
  username: string;
  requestedAt: string;
  status: string;
}

interface NextPayeeResult {
  groupId: string;
  groupName: string;
  recipientId: string;
  recipientUsername: string;
  recipientRole: string;
  currentPosition: number;
  totalMembers: number;
  groupBalance: number;
  lastPayoutRecipient?: string;
  lastPayoutDate?: string;
  payoutFrequency: string;
  nextPayoutDate?: string;
}

class GroupService {
  private baseUrl = '/api/groups';

  async getServiceInfo(): Promise<any> {
    try {
      const response = await authService.apiRequest(this.baseUrl);
      return await response.json();
    } catch (error) {
      console.error('Error getting service info:', error);
      throw error;
    }
  }

  async searchPublicGroups(query?: string): Promise<Group[]> {
    try {
      const url = query ? `${this.baseUrl}/search?query=${encodeURIComponent(query)}` : `${this.baseUrl}/search`;
      const response = await authService.apiRequest(url);
      const data = await response.json();
      return data.groups;
    } catch (error) {
      console.error('Error searching public groups:', error);
      throw error;
    }
  }

  async viewGroup(groupId: string): Promise<{ group: Group; events: any[]; userPermissions: any }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}/view`);
      return await response.json();
    } catch (error) {
      console.error('Error viewing group:', error);
      throw error;
    }
  }

  async createGroup(request: CreateGroupRequest): Promise<{ message: string; groupId: string; eventId: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/create`, {
        method: 'POST',
        body: JSON.stringify(request),
      });
      return await response.json();
    } catch (error) {
      console.error('Error creating group:', error);
      throw error;
    }
  }

  async updateGroup(groupId: string, request: UpdateGroupRequest): Promise<Group> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}`, {
        method: 'PUT',
        body: JSON.stringify(request),
      });
      return await response.json();
    } catch (error) {
      console.error('Error updating group:', error);
      throw error;
    }
  }

  async getUserGroups(): Promise<Group[]> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/user`);
      return await response.json();
    } catch (error) {
      console.error('Error getting user groups:', error);
      throw error;
    }
  }

  async requestToJoinGroup(groupId: string): Promise<{ message: string; groupId: string; eventId: string; status: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}/join`, {
        method: 'GET',
      });
      return await response.json();
    } catch (error) {
      console.error('Error requesting to join group:', error);
      throw error;
    }
  }

  async getGroupJoinRequests(groupId: string): Promise<{ groupId: string; groupName: string; requests: JoinRequest[]; totalPendingRequests: number }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}/requests`, {
        method: 'GET',
      });
      return await response.json();
    } catch (error) {
      console.error('Error getting group join requests:', error);
      throw error;
    }
  }

  async processJoinRequest(groupId: string, requestId: string, action: 'accept' | 'reject'): Promise<{ message: string; groupId: string; requestId: string; action: string; eventId: string; result: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}/request`, {
        method: 'POST',
        body: JSON.stringify({ requestId, action }),
      });
      return await response.json();
    } catch (error) {
      console.error('Error processing join request:', error);
      throw error;
    }
  }

  async getNextPayee(groupId: string): Promise<NextPayeeResult> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}/next-payee`, {
        method: 'GET',
      });
      return await response.json();
    } catch (error) {
      console.error('Error getting next payee:', error);
      throw error;
    }
  }

  async recordPayout(groupId: string, recipientId: string, amount: number): Promise<{ message: string; processedRecipient: string; processedAmount: number; nextPayee: any }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/${groupId}/record-payout`, {
        method: 'POST',
        body: JSON.stringify({ recipientId, amount }),
      });
      return await response.json();
    } catch (error) {
      console.error('Error recording payout:', error);
      throw error;
    }
  }

  async joinOrCreateStokvel(tier: number): Promise<{ message: string; groupId: string }> {
    try {
      const response = await authService.apiRequest(`${this.baseUrl}/join-tier?tier=${tier}`, {
        method: 'POST',
      });
      return await response.json();
    } catch (error) {
      console.error('Error joining/creating stokvel:', error);
      throw error;
    }
  }
}

const groupService = new GroupService();
export default groupService;