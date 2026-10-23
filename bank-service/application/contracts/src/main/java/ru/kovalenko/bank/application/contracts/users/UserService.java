package ru.kovalenko.bank.application.contracts.users;

import ru.kovalenko.bank.application.contracts.users.operations.AddFriend;
import ru.kovalenko.bank.application.contracts.users.operations.CreateUser;
import ru.kovalenko.bank.application.contracts.users.operations.GetAllUsers;
import ru.kovalenko.bank.application.contracts.users.operations.GetFriends;
import ru.kovalenko.bank.application.contracts.users.operations.GetUserById;
import ru.kovalenko.bank.application.contracts.users.operations.GetUsersByFilter;
import ru.kovalenko.bank.application.contracts.users.operations.RemoveFriend;

public interface UserService {
    CreateUser.Response createUser(CreateUser.Request request);

    GetUserById.Response getUserById(GetUserById.Request request);

    GetAllUsers.Response getAllUsers(GetAllUsers.Request request);

    GetUsersByFilter.Response getUsersByFilter(GetUsersByFilter.Request request);

    AddFriend.Response addFriend(AddFriend.Request request);

    RemoveFriend.Response removeFriend(RemoveFriend.Request request);

    GetFriends.Response getFriends(GetFriends.Request request);
}
